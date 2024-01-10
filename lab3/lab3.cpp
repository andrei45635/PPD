#include <stdio.h>
#include <mpi.h> 
#include <stdlib.h> 
#include <iostream>
#include <fstream>
#include <algorithm>
#include <cmath>
#include <chrono>

const int N = 1000, M = 1000, n = 3, m = 3;
int kernel[n][m];
int matrix[N][M];
int result[N][M];

int convolution(int x, int y, int lineOffset, int columnOffset) {
	int output = 0;
	for (int i = 0; i < n; i++) {
		for (int j = 0; j < m; j++) {
			//calculating neighbors
			int k = x - lineOffset + i;
			int t = y - columnOffset + j;

			//out of bounds - line
			if (k < 0) k = 0;
			else if (k >= N) k = N - 1;

			//out of bounds - column
			if (t < 0) t = 0;
			else if (t >= M) t = M - 1;

			output += matrix[k][t] * kernel[i][j];
		}
	}
	return output;
}

int main()
{
	int myid, numprocs, namelen;
	char processor_name[MPI_MAX_PROCESSOR_NAME];

	MPI_Status status;
	MPI_Init(NULL, NULL);
	MPI_Comm_rank(MPI_COMM_WORLD, &myid);  
	MPI_Comm_size(MPI_COMM_WORLD, &numprocs);      
	MPI_Get_processor_name(processor_name, &namelen);

	if (myid == 0) {
		//auto t_start = std::chrono::high_resolution_clock::now();
	
		int lines = N / (numprocs - 1);

		std::ifstream fin("C:\\Users\\GIGABYTE\\IdeaProjects\\PPD\\ppd_lab2_tema\\src\\main\\resources\\lab3_input.txt");

		if (fin.is_open()) {
			for (int i = 0; i < n; i++) {
				for (int j = 0; j < m; j++) {
					fin >> kernel[i][j];
				}
			}
		}

		fin.close();

		MPI_Bcast(kernel, n * m, MPI_INT, 0, MPI_COMM_WORLD);

		std::ifstream in("C:\\Users\\GIGABYTE\\IdeaProjects\\PPD\\ppd_lab2_tema\\src\\main\\resources\\lab3_matrix_input.txt");

		for (int i = 0; i <= N; i++) {
			if (i != N) {
				for (int j = 0; j < M; j++) {
					in >> matrix[i][j];
				}
			}
			if (i % lines == 0 && i != 0) {
				int chunk = i / lines;
				MPI_Send(matrix + std::max(((i / lines) - 1) * lines - 1, 0), (lines + 2) * M, MPI_INT, i / lines, 0, MPI_COMM_WORLD);
			}
		}

		in.close();

		std::ofstream out("C:\\Users\\GIGABYTE\\IdeaProjects\\PPD\\ppd_lab2_tema\\src\\main\\resources\\output_lab3.txt");

		for (int i = 1; i < numprocs; i++) {
			MPI_Recv(result + (i - 1) * lines, lines * M, MPI_INT, i, 0, MPI_COMM_WORLD, &status);
			for (int j = (i - 1) * lines; j < i * lines; j++) {
				for (int k = 0; k < M; k++) {
					out << result[j][k] << " ";
				}
				out << std::endl;
			}

		}

		out.close();

		/*auto t_end = std::chrono::high_resolution_clock::now();
		double elapsed_time_ms = std::chrono::duration<double, std::milli>(t_end - t_start).count();
		std::cout << elapsed_time_ms << std::endl;*/
	}
	else {

		MPI_Bcast(kernel, n * m, MPI_INT, 0, MPI_COMM_WORLD);

		int lines = N / (numprocs - 1);

		MPI_Recv(matrix + std::max((myid - 1) * lines - 1, 0), (lines + 2) * M, MPI_INT, 0, 0, MPI_COMM_WORLD, &status);

		for (int i = (myid - 1) * lines; i < myid * lines; i++) {
			for (int j = 0; j < M; j++) {
				result[i][j] = convolution(i, j, 1, 1);
			}
		}

		MPI_Send(result + (myid - 1) * lines, lines * M, MPI_INT, 0, 0, MPI_COMM_WORLD);
	}

	MPI_Finalize();
}
