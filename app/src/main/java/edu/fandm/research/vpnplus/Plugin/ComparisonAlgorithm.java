package edu.fandm.research.vpnplus.Plugin;

public class ComparisonAlgorithm
{
    private static int NO_OF_CHARS = 256;

    //The preprocessing function for Boyer-Moore's bad character heuristic
    private static void badCharHeuristic(char[] str, int size, int[] badchar)
    {
        // Initialize all occurrences as -1
        for (int i = 0; i < NO_OF_CHARS; i++)
        {
            badchar[i] = -1;
        }

        //Get the index of the last occurrence of a character
        for (int j = 0; j < size; j++)
        {
            badchar[(int) str[j]] = j;
        }
    }

    /* A pattern searching function that uses Bad
       Character Heuristic of Boyer Moore Algorithm */
    public static Boolean search(String text, String pattern)
    {
        char[] txt = text.toCharArray();
        char[] pat = pattern.toCharArray();

        int m = pat.length;
        int n = txt.length;

        int[] badchar = new int[NO_OF_CHARS];
        badCharHeuristic(pat, m, badchar);

        int s = 0; // s is shift of the pattern with respect to text

        while(s <= (n - m))
        {
            int j = m-1;

            int lastIndex = s+j;

	    /* Keep reducing index j of pattern while
	       characters of pattern and text are
	       matching at this shift s */
            while(j >= 0 && pat[j] == txt[s+j])
            {
                j--;
            }

	    /* If the pattern is present at current
	       shift, then index j will become -1 after
	       the above loop */
            if (j < 0)
            {
                return true;

                //possibleIndices.add(s);

		/* Shift the pattern so that the next
		   character in text aligns with the last
		   occurrence of it in pattern.
		   The condition s+m < n is necessary for
		   the case when pattern occurs at the end
		   of text */
                //s += (s+m < n)? m-badchar[txt[s+m]] : 1;
            }
            else
            {
                double similarity = similarity(pattern, text, lastIndex - (m-1), lastIndex);

                if(similarity >= 0.75)
                {
                    return true;

                    //possibleIndices.add(s);

                    //s += (s+m < n)? m-badchar[txt[s+m]] : 1;
                }
                else if(similarity >= 0.5)
                {
                    s += 1;
                }
                else
                {
                    s += (m/2);
                }

		/* Shift the pattern so that the bad character
		   in text aligns with the last occurrence of
		   it in pattern. The max function is used to
		   make sure that we get a positive shift.
		   We may get a negative shift if the last
		   occurrence of bad character in pattern
		   is on the right side of the current
		   character. */
                //s += max(1, j - badchar[txt[s+j]]);
            }
        }

	/*
	if(possibleIndices.isEmpty())
	{
	    possibleIndices.add(-1);
	}
	*/

        return false;
    }

    private static double similarity(String pattern, String text, int startIndex, int lastIndex)
    {
        String packetData = text.substring(startIndex, lastIndex+1);

        int editDist = editDistance(pattern, packetData);

        return (packetData.length() - editDist) / (double) packetData.length();
    }

    private static int getMin(int x,int y,int z)
    {
        if(x <= y && x <= z)
        {
            return x;
        }
        if(y <= x && y <= z)
        {
            return y;
        }
        else
        {
            return z;
        }
    }

    private static int editDistance(String input1, String input2)
    {
        int input1Length = input1.length();
        int input2Length = input2.length();

        int[][] resultTable = new int[input1Length + 1][input2Length + 1];

        for(int i = 0; i <= input1Length; i++)
        {
            for(int j = 0; j <= input2Length; j++)
            {
                if(i == 0)
                {
                    resultTable[i][j] = j;
                }
                else if(j == 0)
                {
                    resultTable[i][j] = i;
                }
                else if(input1.charAt(i-1) == input2.charAt(j-1))
                {
                    resultTable[i][j] = resultTable[i-1][j-1];
                }
                else
                {
                    resultTable[i][j] = 1 + getMin(resultTable[i][j-1], resultTable[i-1][j], resultTable[i-1][j-1]);
                }
            }
        }

        return resultTable[input1Length][input2Length];
    }
}
