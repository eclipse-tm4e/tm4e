#include <iostream>

// CUDA kernel function
__global__ void addKernel(int *a, int *b, int *c, int size) {
  int i = blockIdx.x * blockDim.x + threadIdx.x;
  if (i < size) {
    c[i] = a[i] + b[i];
  }
}

int main() {
  const int size = 5;

  // Host arrays
  int h_a[size] = {1, 2, 3, 4, 5};
  int h_b[size] = {5, 4, 3, 2, 1};
  int h_c[size];

  // Device arrays
  int *d_a, *d_b, *d_c;
  cudaMalloc((void**)&d_a, size * sizeof(int));
  cudaMalloc((void**)&d_b, size * sizeof(int));
  cudaMalloc((void**)&d_c, size * sizeof(int));

  // Copy data from host to device
  cudaMemcpy(d_a, h_a, size * sizeof(int), cudaMemcpyHostToDevice);
  cudaMemcpy(d_b, h_b, size * sizeof(int), cudaMemcpyHostToDevice);

  // Launch kernel
  addKernel<<<1, size>>>(d_a, d_b, d_c, size);

  // Copy result from device to host
  cudaMemcpy(h_c, d_c, size * sizeof(int), cudaMemcpyDeviceToHost);

  // Output result
  std::cout << "Result: ";
  for (int i = 0; i < size; ++i) {
    std::cout << h_c[i] << " ";
  }
  std::cout << std::endl;

  // Free device memory
  cudaFree(d_a);
  cudaFree(d_b);
  cudaFree(d_c);

  return 0;
}
