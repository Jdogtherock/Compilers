// In this test case, each function has a triple nested loop
// that prints out the loop index values. You can use your
// LLVM for loop pass to optimize or modify these loops.

#include <iostream>

void functionA() {
    for (int i = 0; i < 5; i++) {
        for (int j = 0; j < 5; j++) {
        }

        for (int k = 0; k < 5; k++) {

        }
    }
}

void functionB() {
    for (int i = 0; i < 3; i++) {
        for (int j = 0; j < 3; j++) {
            for (int k = 0; k < 3; k++) {
                std::cout << "Function B: " << i << " " << j << " " << k << std::endl;
            }
        }
    }
}

void functionC() {
    for (int i = 0; i < 4; i++) {
        for (int j = 0; j < 4; j++) {
            for (int k = 0; k < 4; k++) {
                std::cout << "Function C: " << i << " " << j << " " << k << std::endl;
            }
        }
    }
}

void functionD() {
    for (int i = 0; i < 2; i++) {
        for (int j = 0; j < 2; j++) {
            for (int k = 0; k < 2; k++) {
                std::cout << "Function D: " << i << " " << j << " " << k << std::endl;
            }
        }
    }
}

void functionE() {
    for (int i = 0; i < 6; i++) {
        for (int j = 0; j < 6; j++) {
            for (int k = 0; k < 6; k++) {
                std::cout << "Function E: " << i << " " << j << " " << k << std::endl;
            }
        }
    }
}

void functionF() {
    for (int i = 0; i < 10; i++) {
        for (int j = 0; j < 10; j++) {
            for (int k = 0; k < 10; k++) {
                std::cout << "Function F: " << i << " " << j << " " << k << std::endl;
            }
        }
    }
}

int main() {
    functionA();
    functionB();
    functionC();
    functionD();
    functionE();
    functionF();
    return 0;
}
