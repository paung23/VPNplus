package edu.fandm.research.vpnplus.Plugin;

import android.util.Log;

/**
 * Created by Phyo Thuta Aung
 */
public class ComparisonAlgorithm
{
    /* A pattern searching function that uses Bad Character Heuristic of Boyer Moore Algorithm */
    public static Boolean search(String text, String pattern, String category)
    {

        Log.d("enovak.vpnplus.compare", "Searching For '" + category + "'  with value: " + pattern );
        char[] txt = text.toCharArray();
        char[] pat = pattern.toCharArray();

        int m = pat.length;
        int n = txt.length;

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

            }
            else
            {
                double similarity = similarity(pattern, text, lastIndex - (m-1), lastIndex);

                if(similarity >= 0.75)
                {
                    return true;
                }
                else if(similarity >= 0.5)
                {
                    s += 1;
                }
                else
                {
                    s += (m/2);
                }
            }
        }

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


    // Levenshtein Distance
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