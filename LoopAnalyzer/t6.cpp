// Program 6: Nested loops in a function

#include <iostream>

void printNumbers()
{
    for (int i = 0; i < 10; i++)
    {
        for (int j = 0; j < i; j++)
        {
            std::cout << i << " " << j << std::endl;
        }
    }
}

int main()
{
    printNumbers();
    return 0;
}