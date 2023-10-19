// Program 8: Double for loop with continue statement

#include <iostream>

int main()
{
    for (int i = 0; i < 10; i++)
    {
        if (i == 5)
        {
            continue;
        }
        for (int j = 0; j < 5; j++)
        {
            std::cout << i << " " << j << std::endl;
        }
    }
    return 0;
}