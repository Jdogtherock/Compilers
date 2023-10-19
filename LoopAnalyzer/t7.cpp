// Program 7: Single for loop with break statement

#include <iostream>

int main()
{
    for (int i = 0; i < 10; i++)
    {
        if (i == 5)
        {
            break;
        }
        std::cout << i << std::endl;
    }
    return 0;
}