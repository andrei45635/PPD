#include <iostream>
#include <thread>
#include <random>

void sum(int id, int start, int end, const int a[], const int b[], int c[]) {
    for (int i = start; i < end; i++) {
        c[i] = a[i] + b[i];
    }
}

void sumVectors(int id, int start, int end, const std::vector<int>& a, const std::vector<int>& b, std::vector<int> c) {
    for (int i = start; i < end; i++) {
        c[i] = a[i] + b[i];
    }
}

void sumStep(int id, int n, int step, const int a[], const int b[], int c[]) {
    for (int i = id; i < n; i += step) {
        c[i] = a[i] + b[i];
    }
}

void sumStepVectors(int id, int n, int step, const std::vector<int>& a, const std::vector<int>& b, std::vector<int> c) {
    for (int i = id; i < n; i += step) {
        c[i] = a[i] + b[i];
    }
}

void startEndThreads(int n, int THREAD_COUNT, const int a[], const int b[], int c[]) {
    auto t_start = std::chrono::high_resolution_clock::now();

    int start = 0, end;
    int cat = n / THREAD_COUNT;
    int rest = n % THREAD_COUNT;
    std::thread th[16];
    for (int i = 0; i < THREAD_COUNT; i++) {
        end = start + cat;
        if (rest > 0) {
            end++;
            rest--;
        }
        th[i] = std::thread(sum, i, start, end, a, b, c); //created directly on the heap -> threads in C++ are faster than the threads in Java
        start = end;
    }

    for (int i = 0; i < THREAD_COUNT; i++) {
        th[i].join();
    }

    auto t_end = std::chrono::high_resolution_clock::now();
    double elapsed_time_ms = std::chrono::duration<double, std::milli>(t_end - t_start).count();

    std::cout << "elapsed_time_ms = " << elapsed_time_ms << "\n";
}

void startEndThreadsVector(int n, int THREAD_COUNT, const std::vector<int>& a, const std::vector<int>& b, std::vector<int> c) {
    auto t_start = std::chrono::high_resolution_clock::now();

    int start = 0, end;
    int cat = n / THREAD_COUNT;
    int rest = n % THREAD_COUNT;
    std::thread th[16];
    for (int i = 0; i < THREAD_COUNT; i++) {
        end = start + cat;
        if (rest > 0) {
            end++;
            rest--;
        }
        th[i] = std::thread(sumVectors, i, start, end, a, b, c); //created directly on the heap -> threads in C++ are faster than the threads in Java
        start = end;
    }

    for (int i = 0; i < THREAD_COUNT; i++) {
        th[i].join();
    }

    auto t_end = std::chrono::high_resolution_clock::now();
    double elapsed_time_ms = std::chrono::duration<double, std::milli>(t_end - t_start).count();

    std::cout << "elapsed_time_ms_vectors = " << elapsed_time_ms << "\n";
}

void stepThreads(int n, int THREAD_COUNT, const int a[], const int b[], int c[]) {
    auto t_start_step = std::chrono::high_resolution_clock::now();
    std::thread th2[16];
    for (int i = 0; i < THREAD_COUNT; i++) {
        th2[i] = std::thread(sumStep, i, n, 16, a, b, c); //created directly on the heap -> threads in C++ are faster than the threads in Java
    }

    for (int i = 0; i < THREAD_COUNT; i++) {
        th2[i].join();
    }

    auto t_end_step = std::chrono::high_resolution_clock::now();
    double elapsed_time_ms_step = std::chrono::duration<double, std::milli>(t_end_step - t_start_step).count();

    std::cout << "elapsed_time_ms_step = " << elapsed_time_ms_step << "\n";
}

void stepThreadsVectors(int n, int THREAD_COUNT, const std::vector<int>& a, const std::vector<int>& b, std::vector<int> c) {
    auto t_start_step = std::chrono::high_resolution_clock::now();
    std::thread th2[16];
    for (int i = 0; i < THREAD_COUNT; i++) {
        th2[i] = std::thread(sumStepVectors, i, n, 16, a, b, c); //created directly on the heap -> threads in C++ are faster than the threads in Java
    }

    for (int i = 0; i < THREAD_COUNT; i++) {
        th2[i].join();
    }

    auto t_end_step = std::chrono::high_resolution_clock::now();
    double elapsed_time_ms_step = std::chrono::duration<double, std::milli>(t_end_step - t_start_step).count();

    std::cout << "elapsed_time_ms_step_vectors = " << elapsed_time_ms_step << "\n";
}

int main() {
    const int THREAD_COUNT = (int)std::thread::hardware_concurrency();
    const int n = 1e5;

    int a[n], b[n], c[n];
    std::vector<int> vectA, vectB, vectC;

    for (int i = 0; i < n; i++) {
        a[i] = std::rand() % n + 1;
        b[i] = std::rand() % n + 1;
        c[i] = 0;
    }

    for (int i = 0; i < n; i++) {
        vectA.push_back(std::rand() % n + 1);
        vectB.push_back(std::rand() % n + 1);
        vectC.push_back(0);
    }

    startEndThreads(n, THREAD_COUNT, a, b, c);
    startEndThreadsVector(n, THREAD_COUNT, vectA, vectB, vectC);

    for (int i = 0; i < n; i++) {
        c[i] = 0;
    }

    stepThreads(n, THREAD_COUNT, a, b, c);
    stepThreadsVectors(n, THREAD_COUNT, vectA, vectB, vectC);

    return 0;
}