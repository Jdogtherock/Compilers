// Program 5: Multiple loops in a function

#include <iostream>

void printNumbers()
{
    for (int i = 0; i < 10; i++)
    {
        std::cout << i << std::endl;
    }
    for (int j = 0; j < 5; j++)
    {
        std::cout << j << std::endl;
    }
}

int main()
{
    printNumbers();
    return 0;
}