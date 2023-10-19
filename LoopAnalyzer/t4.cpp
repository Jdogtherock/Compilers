// Program 4: Loop in a function

#include <iostream>

void printNumbers()
{
    for (int i = 0; i < 10; i++)
    {
        std::cout << i << std::endl;
    }
}

int main()
{
    printNumbers();
    return 0;
}