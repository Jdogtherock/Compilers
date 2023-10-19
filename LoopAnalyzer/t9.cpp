// Program 9: Nested for loop with return statement

#include <iostream>

int printNumbers()
{
    for (int i = 0; i < 10; i++)
    {
        for (int j = 0; j < i; j++)
        {
            std::cout << i << " " << j << std::endl;
            if (i == 5 && j == 2)
            {
                return 1;
            }
        }
    }
    return 0;
}

int main()
{
    printNumbers();
}