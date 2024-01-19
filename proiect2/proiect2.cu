#include <cuda_runtime.h>
#include <device_launch_parameters.h>

#include<iostream>
#include<fstream>
#include<cstdlib>
#include<chrono>
#include<condition_variable>
#include<vector>

#define KERNEL_SIZE 3
#define MAX_THREADS_PER_BLOCK 1024

typedef struct{
    int * L_frontier;
    int * R_frontier;
}side_frontiers;

__device__ __host__
int clip(int x, int a, int b){
	// clips x between a and b
	return x < a ? a : (x > b ? b : x);
}

void cudaCheckError(cudaError_t cudaResult){
    if (cudaResult != cudaSuccess) {
        std::cerr << "CUDA error: " << cudaGetErrorString(cudaResult) << std::endl;
        exit(1);
    }
}

__global__
void convolution(
int N, int start, int end,
int ** matrix, int ** kernel,
int * N_frontier, int * S_frontier,
int * current_line, // stores the index of the line in the matrix that is being processed currently
side_frontiers side_frontiers,
int grid_index
){
    
    int i = start + blockIdx.x;
    int j = MAX_THREADS_PER_BLOCK * grid_index + threadIdx.x;

    // if(threadIdx.x == 0 && blockIdx.x == 0){
    //     printf("%d %d %d %d %d %d\n", start, end, i, j, *current_line, blockDim.x);
    // }

    while(i != atomicAdd(current_line, 0)); // spin lock until current_line is equal to the block's line

    int NW_value;
    int N_value;
    int NE_value;
    int W_value;
    int center_value;
    int E_value;
    int SW_value;
    int S_value;
    int SE_value;

    N_value      = atomicAdd(&N_frontier[j], 0);
    center_value = atomicAdd(&matrix[i][j], 0);

    int i_sub_1 = clip(i - 1, 0, N - 1);
    int i_add_1 = clip(i + 1, 0, N - 1);
    int j_sub_1 = clip(j - 1, 0, N - 1);
    int j_add_1 = clip(j + 1, 0, N - 1);

    if(threadIdx.x == 0){
        NW_value = side_frontiers.L_frontier[i_sub_1];
        W_value  = side_frontiers.L_frontier[i];
        SW_value = side_frontiers.L_frontier[i_add_1];

        NE_value = atomicAdd(&N_frontier[j_add_1], 0);
        E_value  = atomicAdd(&matrix[i][j_add_1], 0);

        if(i == end - 1){
            S_value  = S_frontier[j];
            SE_value = S_frontier[j_add_1];
        }
        else{
            S_value  = atomicAdd(&matrix[i_add_1][j], 0);
            SE_value = atomicAdd(&matrix[i_add_1][j_add_1], 0);
        }
    }
    else if(threadIdx.x == blockDim.x - 1){
        NE_value = side_frontiers.R_frontier[i_sub_1];
        E_value  = side_frontiers.R_frontier[i];
        SE_value = side_frontiers.R_frontier[i_add_1];

        NW_value = atomicAdd(&N_frontier[j_sub_1], 0);
        W_value  = atomicAdd(&matrix[i][j_sub_1], 0);

        if(i == end - 1){
            S_value  = S_frontier[j];
            SW_value = S_frontier[j_sub_1]; 
        }
        else{
            S_value  = atomicAdd(&matrix[i_add_1][j], 0);
            SW_value = atomicAdd(&matrix[i_add_1][j_sub_1], 0);
        }
    }
    else{
        NW_value = atomicAdd(&N_frontier[j_sub_1], 0);
        NE_value = atomicAdd(&N_frontier[j_add_1], 0);

        W_value = atomicAdd(&matrix[i][j_sub_1], 0);
        E_value = atomicAdd(&matrix[i][j_add_1], 0);

        if(i == end - 1){
            SW_value = S_frontier[j_sub_1];
            S_value  = S_frontier[j];
            SE_value = S_frontier[j_add_1];
        }
        else{
            SW_value = atomicAdd(&matrix[i_add_1][j_sub_1], 0);
            S_value  = atomicAdd(&matrix[i_add_1][j], 0);
            SE_value = atomicAdd(&matrix[i_add_1][j_add_1], 0);
        }
    }

    __syncthreads();
    
    // convolution operation
    atomicExch(&matrix[i][j], (
        NW_value * kernel[0][0] +
        N_value  * kernel[0][1] +
        NE_value * kernel[0][2] +

        W_value      * kernel[1][0] +
        center_value * kernel[1][1] +
        E_value      * kernel[1][2] + 

        SW_value * kernel[2][0] +
        S_value  * kernel[2][1] +
        SE_value * kernel[2][2]
        ) / 9
    );

    // make sure all the threads have updated the auxiliary arrays before starting next block
    atomicExch(&N_frontier[j], center_value);

    __syncthreads();
    
    // block is done with computation, increment current_line and the next block should start
    if(threadIdx.x == 0){
        atomicAdd(current_line, 1);
    }
}

int main(int argc, char ** argv){
/*
arg 1 input matrix
arg 2 kernel
arg 3 number of threads
arg 4 width and height of input matrix
arg 5 correct output file
arg 6 output file
*/
    if(argc < 7){
		std::cerr << "not enough args" << std::endl;
		exit(1);
	}
	std::ifstream f(argv[1]);
	if(!f.is_open()){
		std::cerr << "could not open file " << argv[1] << std::endl;
		exit(1);
	}
	std::ifstream k(argv[2]);
	if(!k.is_open()){
		std::cerr << "could not open file " << argv[2] << std::endl;
		exit(1);
	}

    const int P = atoi(argv[3]);
    const int N = atoi(argv[4]);

    int ** F = new int * [N];
    int ** K = new int * [KERNEL_SIZE];

    // read input and kernel matrix
    for(int i = 0; i < N; i++){
        F[i] = new int [N];
        for(int j = 0; j < N; j++){
            f >> F[i][j];
        }
    }
    for(int i = 0; i < KERNEL_SIZE; i++){
        K[i] = new int [KERNEL_SIZE];
        for(int j = 0; j < KERNEL_SIZE; j++){
            k >> K[i][j];
        }
    }
    f.close();
    k.close();

    // allocate memory and copy data to GPU
    int ** device_F;
    int ** device_K;
    int ** host_pointers_F; /* arrays on the host to */
    int ** host_pointers_K; /* store device pointers */
    host_pointers_F = new int * [N];
    host_pointers_K = new int * [KERNEL_SIZE];
    cudaCheckError(cudaMalloc((void ***)&device_F, N * sizeof(int *)));
    cudaCheckError(cudaMalloc((void ***)&device_K, KERNEL_SIZE * sizeof(int *)));
    for(int i = 0; i < N; i++){
        cudaCheckError(cudaMalloc((void **)&host_pointers_F[i], N * sizeof(int *)));
        cudaCheckError(cudaMemcpy(host_pointers_F[i], F[i], N * sizeof(int), cudaMemcpyHostToDevice));
    }
    cudaCheckError(cudaMemcpy(device_F, host_pointers_F, N * sizeof(int *), cudaMemcpyHostToDevice));
    for(int i = 0; i < KERNEL_SIZE; i++){
        cudaCheckError(cudaMalloc((void **)&host_pointers_K[i], KERNEL_SIZE * sizeof(int *)));
        cudaCheckError(cudaMemcpy(host_pointers_K[i], K[i], KERNEL_SIZE * sizeof(int), cudaMemcpyHostToDevice));
    }
    cudaCheckError(cudaMemcpy(device_K, host_pointers_K, KERNEL_SIZE * sizeof(int *), cudaMemcpyHostToDevice));
    
    int columns_remaining = N;
    int column = 0;
    std::vector<side_frontiers> side_frontiers_vector;
    while(columns_remaining > 0){
        side_frontiers side_frontier;
        int * device_Lfrontier;
        int * device_Rfrontier;
        int * host_Lfrontier = new int [N];
        int * host_Rfrontier = new int [N];

        int Lcolumn_index = clip(column - 1, 0, N - 1);
        int Rcolumn_index = clip(column + MAX_THREADS_PER_BLOCK, 0, N -1);

        // std::cout << Lcolumn_index << " " << Rcolumn_index << std::endl;
        
        for(int i = 0; i < N; i++){
            host_Lfrontier[i] = F[i][Lcolumn_index];
            host_Rfrontier[i] = F[i][Rcolumn_index];
        }

        cudaCheckError(cudaMalloc((void **)&device_Lfrontier, N * sizeof(int)));
        cudaCheckError(cudaMalloc((void **)&device_Rfrontier, N * sizeof(int)));

        cudaCheckError(cudaMemcpy(device_Lfrontier, host_Lfrontier, N * sizeof(int), cudaMemcpyHostToDevice));
        cudaCheckError(cudaMemcpy(device_Rfrontier, host_Rfrontier, N * sizeof(int), cudaMemcpyHostToDevice));

        side_frontier.L_frontier = device_Lfrontier;
        side_frontier.R_frontier = device_Rfrontier;
        side_frontiers_vector.push_back(side_frontier);

        delete host_Lfrontier;
        delete host_Rfrontier;

        column += MAX_THREADS_PER_BLOCK;
        columns_remaining -= MAX_THREADS_PER_BLOCK;
    }

    // arrays to store device pointers to auxiliary arrays and sync mechanisms
    int ** N_frontiers   = new int * [P];
    int ** S_frontiers   = new int * [P];
    int ** current_lines = new int * [P * side_frontiers_vector.size()];

    int rest = N % P;
    auto t_start = std::chrono::high_resolution_clock::now();
    
    // convolution computation
    for(int i = 0; i < P; i++){

        int start = i * (N / P) + (N % P - rest);
        int end = start + N / P;
        if(rest > 0){
            end++;
            rest--;
        }

        int * N_frontier;
        int * S_frontier;
        int * current_line;
        int host_init_current_line_value = start;

        // allocate and initialize auxiliary arrays
        cudaCheckError(cudaMalloc((void **)&N_frontier, N * sizeof(int)));
        cudaCheckError(cudaMalloc((void **)&S_frontier, N * sizeof(int)));
        cudaCheckError(cudaMemcpy(N_frontier, F[clip(start - 1, 0, N - 1)], N * sizeof(int), cudaMemcpyHostToDevice));
        cudaCheckError(cudaMemcpy(S_frontier, F[clip(end, 0, N - 1)], N * sizeof(int), cudaMemcpyHostToDevice));

        // store allocated pointers
        N_frontiers[i] = N_frontier;
        S_frontiers[i] = S_frontier;

        int columns_left = N;
        int side_frontier_index = 0;
        while(columns_left > 0){

            // allocate and initialize inter-block synchronization tools
            cudaCheckError(cudaMalloc((void **)&current_line, sizeof(int)));
            cudaCheckError(cudaMemcpy(current_line, &host_init_current_line_value, sizeof(int), cudaMemcpyHostToDevice));
            // save in array for later de-allocation
            current_lines[i * side_frontiers_vector.size() + side_frontier_index] = current_line;

            int grid_index = side_frontier_index;
            if(columns_left >= MAX_THREADS_PER_BLOCK){
                convolution<<<end-start, MAX_THREADS_PER_BLOCK>>>(
                    N, start, end,
                    device_F, device_K,
                    N_frontier, S_frontier,
                    current_line,
                    side_frontiers_vector[side_frontier_index],
                    grid_index
                );
            }
            else{
                convolution<<<end-start, columns_left>>>(
                    N, start, end,
                    device_F, device_K,
                    N_frontier, S_frontier,
                    current_line,
                    side_frontiers_vector[side_frontier_index],
                    grid_index
                );
            }
        
            cudaError_t cudaError = cudaGetLastError();
            if(cudaError != cudaSuccess){
                std::cerr << cudaGetErrorName(cudaError) << " " << cudaGetErrorString(cudaError);
            }

            columns_left -= MAX_THREADS_PER_BLOCK;
            side_frontier_index++;
        }
    }
    cudaDeviceSynchronize();
    for(int i = 0; i < N; i++){
        cudaCheckError(cudaMemcpy(F[i], host_pointers_F[i], N * sizeof(int), cudaMemcpyDeviceToHost));
    }

    // get elapsed time
    auto t_end = std::chrono::high_resolution_clock::now();
    double elapsed_time_ms = std::chrono::duration<double, std::milli>(t_end - t_start).count();
    std::cout << elapsed_time_ms << std::endl;

    // write to output file
    std::ofstream g(argv[6]);
    for(int i = 0; i < N; i++){
        for(int j = 0; j < N; j++){
            g << F[i][j] << " ";
        }
        g << std::endl;
    }
    g.close();

    // free memory
    // matrix
    for(int i = 0; i < N; i++){
        cudaCheckError(cudaFree(host_pointers_F[i]));
        delete F[i];
    }
    cudaCheckError(cudaFree(device_F));
    delete host_pointers_F;
    delete F;
    // kernel
    for(int i = 0; i < KERNEL_SIZE; i++){
        cudaCheckError(cudaFree(host_pointers_K[i]));
        delete K[i];
    }
    cudaCheckError(cudaFree(device_K));
    delete host_pointers_K;
    delete K;
    // auxiliary arrays and sync mechanisms
    for(int i = 0; i < P; i++){
        cudaCheckError(cudaFree(N_frontiers[i]));
        cudaCheckError(cudaFree(S_frontiers[i]));
    }
    for(int i = 0; i < P * side_frontiers_vector.size(); i++){
        cudaCheckError(cudaFree(current_lines[i]));
    }
    delete N_frontiers;
    delete S_frontiers;
    delete current_lines;
    for(int i = 0; i < side_frontiers_vector.size(); i++){
        cudaCheckError(cudaFree(side_frontiers_vector[i].L_frontier));
        cudaCheckError(cudaFree(side_frontiers_vector[i].R_frontier));
    }

    // error check
    std::ifstream correct(argv[5]);
    if(!correct.is_open()){
		std::cerr << "could not open file " << argv[5] << std::endl;
		exit(1);
	}
    std::ifstream output(argv[6]);
    if(!output.is_open()){
		std::cerr << "could not open file " << argv[6] << std::endl;
		exit(1);
	}
    int x,y;
    while(correct >> x){
        if(output >> y){
            if(x != y){
                std::cerr << "Incorrect output!" << std::endl;
                std::cerr << "Diff found: " << x << " " << y << std::endl;
                exit(1);
            }
        }
        else{
            std::cerr << "Incorrect output!" << std::endl;
            std::cerr << "Not enough elements in output" << std::endl;
            exit(1);
        }
    }
    if(output >> y){
        std::cerr << "Incorrect output!" << std::endl;
        std::cerr << "Too many elements in output" << std::endl;
        exit(1);
    }
    std::cout << "Correct output!" << std::endl;

    return 0;
}
