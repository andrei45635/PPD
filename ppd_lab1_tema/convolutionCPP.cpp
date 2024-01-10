#include <iostream>
#include <fstream>
#include <thread>
#include <string>

#define N 10000
#define M 10
#define n 5
#define m 5

//static
/*int matrix[N][M];
int kernel[n][m];
int result[N][M];*/

int** matrix;
int** kernel;
int** result;

int n1, m1, N1, M1;

//static
//int convolution(int x, int y, int lineOffset, int columnOffset, int matrix[N][M], int kernel[n][m]) {
int convolution(int x, int y, int lineOffset, int columnOffset, int** matrix, int** kernel) {
    int output = 0;
    for (int i = 0; i < n; i++) {
        for (int j = 0; j < m; j++) {
            //calculating neighbors
            int ii = x - lineOffset + i;
            int jj = y - columnOffset + j;

            //out of bounds - line
            if (ii < 0) ii = 0;
            else if (ii >= N) ii = N - 1;

            //out of bounds - column
            if (jj < 0) jj = 0;
            else if (jj >= M) jj = M - 1;
            //std::cout << "matrix[ii][jj]= " << matrix[ii][jj] << " kernel[i][j]= " << kernel[i][j] << " i= " << i << " j= " << j << " ii= " << ii << " jj= " << jj << std::endl;
            output += matrix[ii][jj] * kernel[i][j];
        }
    }
    return output;
}


void threadConvolution(int lineOffset, int columnOffset, int start, int end) {
   /*for (int i = start; i < end; i++)
    {
        for (int j = 0; j < M; j++)
        {
            result[i][j] = convolution(i, j, lineOffset, columnOffset, matrix, kernel);
        }
    }*/

    for (int i = 0; i < N; i++)
    {
        for (int j = start; j < end; j++)
        {
            result[i][j] = convolution(i, j, lineOffset, columnOffset, matrix, kernel);
        }
    }
     
    /*if (N > M)
    {
        for (int i = start; i < end; i++)
            for (int j = 0; j < M; j++)
                result[i][j] = convolution(i, j, lineOffset, columnOffset, matrix, kernel);
    }
    else
    {
        for (int i = 0; i < N; i++)
            for (int j = start; j < end; j++)
                result[i][j] = convolution(i, j, lineOffset, columnOffset, matrix, kernel);
    }*/
}

void read(std::string filename) {
    std::ifstream fin(filename);
    if (fin.is_open()) {

        fin >> N1 >> M1;

        for (int i = 0; i < N1; i++) {
            for (int j = 0; j < M1; j++) {
                fin >> matrix[i][j];
            }
        }

        fin >> n1 >> m1;

        for (int i = 0; i < n1; i++) {
            for (int j = 0; j < m1; j++) {
                fin >> kernel[i][j];
            }
        }
    }
    /*for (int i = 0; i < N1; i++) {
        for (int j = 0; j < M1; j++) {
            std::cout << matrix[i][j] << " ";
        }
        std::cout << std::endl;
    }
    std::cout << std::endl;
    for (int i = 0; i < n1; i++) {
        for (int j = 0; j < m1; j++) {
            std::cout << kernel[i][j] << " ";
        }
		std::cout << std::endl;
    }*/
    fin.close();
}

void write(std::string filename) {
    std::ofstream fout(filename);
    if (fout.is_open()) {
        for (int i = 0; i < N1; i++) {
            for (int j = 0; j < M1; j++) {
                fout << result[i][j] << " ";
            }
            fout << std::endl;
        }
    }
    fout.close();
}

void sequential(int lineOffset, int columnOffset) {
    auto startTime = std::chrono::high_resolution_clock::now();

    for (int i = 0; i < N1; i++) {
        for (int j = 0; j < M1; j++) {
            result[i][j] = convolution(i, j, lineOffset, columnOffset, matrix, kernel);
        }
    }

    auto endTime = std::chrono::high_resolution_clock::now();

    double difference = std::chrono::duration<double, std::milli>(endTime - startTime).count();

    std::cout << difference << std::endl;
}

void parallel(int lineOffset, int columnOffset, int THREAD_COUNT) {
    auto t_start = std::chrono::high_resolution_clock::now();

    int start = 0, end;
    int cat = N / THREAD_COUNT;
    int rest = N % THREAD_COUNT; 

    std::thread th[16];
    for (int i = 0; i < THREAD_COUNT; i++) {
        end = start + cat;
        if (rest > 0) {
            end++;
            rest--;
        }
        th[i] = std::thread(threadConvolution, lineOffset, columnOffset, start, end);
        start = end;
    }

    for (int i = 0; i < THREAD_COUNT; i++) {
        th[i].join();
    }

    auto t_end = std::chrono::high_resolution_clock::now();
    double difference = std::chrono::duration<double, std::milli>(t_end - t_start).count();

    std::cout << difference << std::endl;
}

void allocation(){
    matrix = new int* [N1];
    kernel = new int* [n1];
    result = new int* [N1];

    for (int i = 0; i < N1; i++) {
        matrix[i] = new int [M1];
        result[i] = new int [M1];
    }

    for (int i = 0; i < n1; i++) {
        kernel[i] = new int[m1];
    }
}

void destruction() {
    for (int i = 0; i < N1; i++) {
        delete[] matrix[i];
        delete[] result[i];
    }

    delete[] matrix;
    delete[] result;

    for (int i = 0; i < n1; i++) {
        delete[] kernel[i];
    }

    delete[] kernel;
}

int main(int argc, char** argv)
{
    std::string filename = "C:\\Users\\GIGABYTE\\IdeaProjects\\PPD\\ppd_lab1_tema\\src\\main\\resources\\date.txt";
    std::string filenameSizes = "C:\\Users\\GIGABYTE\\IdeaProjects\\PPD\\ppd_lab1_tema\\src\\main\\resources\\marimi.txt";
    std::string filenameOutput = "C:\\Users\\GIGABYTE\\IdeaProjects\\PPD\\ppd_lab1_tema\\src\\main\\resources\\output.txt";

    std::ifstream fin(filenameSizes);
    fin >> N1 >> M1 >> n1 >> m1;
    std::cout << N1 << " " << M1 << " " << n1 << " " << m1 << std::endl;
    allocation(); 

    read(filename);

    for (int i = 0; i < n; i++) {
        for (int j = 0; j < m; j++) {
            std::cout << kernel[i][j] << " ";
        }
        std::cout << std::endl;
    }

    int lineOffset = (n1 - 1) / 2;
    int columnOffset = (m1 - 1) / 2;

    int THREAD_COUNT = atoi(argv[1]);

    if (THREAD_COUNT == 1) sequential(lineOffset, columnOffset);
    else parallel(lineOffset, columnOffset, THREAD_COUNT);

    /*for (int i = 0; i < N1; i++) {
        for (int j = 0; j < M1; j++) {
            std::cout << result[i][j] << " ";
        }
        std::cout << std::endl;
    }*/

    write(filenameOutput);

    destruction();

    return 0;
}
