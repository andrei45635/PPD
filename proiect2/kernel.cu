
#include "cuda_runtime.h"
#include "device_launch_parameters.h"
#include "cuda_runtime_api.h"
#include "cuda.h"
#include "cooperative_groups.h"
#include <cooperative_groups/memcpy_async.h>
#include <cooperative_groups/reduce.h>
#include <cooperative_groups/scan.h>

#include <stdio.h>
#include <iostream>
#include <fstream>
#include <vector>
#include <time.h>
#include <chrono>

__global__
void convolutionKernel(int* matrix, const double* kernel, int n, int m, int kernel_size, int startRow, int endRow) {
    //cooperative_groups::grid_group g = cooperative_groups::this_grid();

    int row = blockIdx.y * blockDim.y + threadIdx.y;
    int col = blockIdx.x * blockDim.x + threadIdx.x;

    if (row >= startRow && row <= endRow && col < m) {
        double temp = 0.0;
        int kernel_radius = kernel_size / 2;
        for (int i = -kernel_radius; i <= kernel_radius; i++) {
            for (int j = -kernel_radius; j <= kernel_radius; j++) {
                int r = __max(0, __min(row + i, n - 1));
                int c = __max(0, __min(col + j, m - 1));
                temp += matrix[r * m + c] * kernel[(i + kernel_radius) * kernel_size + (j + kernel_radius)];
            }
        }
        __syncthreads();
        matrix[row * m + col] = static_cast<int>(temp);
    }

    //g.sync();
}

void readMatrixFromFile(const std::string& filename, std::vector<int>& matrix, int& rows, int& cols) {
    std::ifstream file(filename);
    if (!file) {
        std::cerr << "Error opening file: " << filename << std::endl;
        exit(1);
    }
    
    file >> rows >> cols;
    matrix.resize(rows * cols);
    
    for (int i = 0; i < rows * cols; i++) {
        file >> matrix[i];
    }
}

void writeMatrixToFile(const std::string& filename, const std::vector<int>& matrix, int rows, int cols) {
    std::ofstream file(filename);
    if (!file) {
        std::cerr << "Error opening file for writing: " << filename << std::endl;
        exit(1);
    }

    for (int i = 0; i < rows; ++i) {
        for (int j = 0; j < cols; ++j) {
            file << matrix[i * cols + j] << " ";
        }
        file << "\n";
    }

    file.close();
}

int main()
{
    std::string small_matrix = "C:\\Users\\GIGABYTE\\IdeaProjects\\PPD\\ppd_lab2_tema\\src\\main\\resources\\date.txt";
    std::string huge_matrix = "C:\\Users\\GIGABYTE\\IdeaProjects\\PPD\\ppd_lab2_tema\\src\\main\\resources\\lab3_input.txt";
    std::string huge_matrix_2 = "C:\\Users\\GIGABYTE\\IdeaProjects\\PPD\\ppd_lab2_tema\\src\\main\\resources\\lab3_marimi.txt";
    //double kernel[3][3] = { 5, 1, 15, 21, 12, 43, 52, 69, 88 };
    double kernel[3][3] = { 0, 1, 0, 0, 0, 0, 0, 0, 0 };
    int n = 1000, m = 1000;
    int rows, cols;
    std::vector<int> matrix;
    
    readMatrixFromFile(huge_matrix_2, matrix, rows, cols);

    auto t_start = std::chrono::high_resolution_clock::now();
    
    int* dev_matrix;
    size_t matrix_size = rows * cols * sizeof(int);
    cudaMalloc(&dev_matrix, matrix_size);
    cudaMemcpy(dev_matrix, matrix.data(), matrix_size, cudaMemcpyHostToDevice);

    double* dev_kernel;
    size_t kernel_size = 3 * 3 * sizeof(double);
    cudaMalloc(&dev_kernel, kernel_size);
    cudaMemcpy(dev_kernel, kernel, kernel_size, cudaMemcpyHostToDevice);

    dim3 threadsPerBlock(16, 16);

    int rowsPerSegment = 50;
    int totalSegments = (n / 2) / rowsPerSegment;
    
    for (int seg = 0; seg < totalSegments; seg++) {
        int startRow = seg * rowsPerSegment;
        int endRow = seg * rowsPerSegment;

        dim3 blocksPerGrid((m + threadsPerBlock.x - 1) / threadsPerBlock.x, (rowsPerSegment + threadsPerBlock.y - 1) / threadsPerBlock.y);

        convolutionKernel<<<blocksPerGrid, threadsPerBlock>>>(dev_matrix, dev_kernel, n, m, 3, startRow, endRow);
        cudaDeviceSynchronize();
    }

    cudaMemcpy(matrix.data(), dev_matrix, matrix_size, cudaMemcpyDeviceToHost);

    // Check for any errors in kernel launch
    cudaError_t cudaStatus = cudaGetLastError();
    if (cudaStatus != cudaSuccess) {
        fprintf(stderr, "convolutionKernel launch failed: %s\n", cudaGetErrorString(cudaStatus));
        return 1;
    }

    // Wait for GPU to finish before accessing on host
    cudaStatus = cudaDeviceSynchronize();
    if (cudaStatus != cudaSuccess) {
        fprintf(stderr, "cudaDeviceSynchronize returned error code %d after launching convolutionKernel!\n", cudaStatus);
        return 1;
    }

    // Copy result back to host
    cudaMemcpy(matrix.data(), dev_matrix, matrix_size, cudaMemcpyDeviceToHost);

    // Free GPU memory
    cudaFree(dev_matrix);
    cudaFree(dev_kernel);
    
    auto t_end = std::chrono::high_resolution_clock::now();
    double difference = std::chrono::duration<double, std::milli>(t_end - t_start).count();

    std::cout << difference << std::endl;

    std::string output = "C:\\Users\\GIGABYTE\\IdeaProjects\\PPD\\ppd_lab2_tema\\src\\main\\resources\\output.txt";
    writeMatrixToFile(output, matrix, rows, cols);

    return 0;
}
