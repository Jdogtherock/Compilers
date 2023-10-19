// Program 3: Nested for loop

#include <iostream>

int main()
{
    for (int i = 0; i < 10; i++)
    {
        for (int j = 0; j < i; j++)
        {
            std::cout << i << " " << j << std::endl;
        }
    }
    return 0;
}