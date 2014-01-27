package com.whitebearsolutions.imagine.wbsairback.util;

public class CharacterEncode {
	private static char[][] Characters = new char[14][2];
    static {
    	Characters[0][0] = 'á';
    	Characters[0][1] = 'a';
    	Characters[1][0] = 'é';
    	Characters[1][1] = 'e';
    	Characters[2][0] = 'í';
    	Characters[2][1] = 'i';
    	Characters[3][0] = 'ó';
    	Characters[3][1] = 'o';
    	Characters[4][0] = 'ú';
    	Characters[4][1] = 'u';
    	Characters[5][0] = 'ñ';
    	Characters[5][1] = 'n';
    	Characters[6][0] = 'Á';
    	Characters[6][1] = 'A';
    	Characters[7][0] = 'É';
    	Characters[7][1] = 'E';
    	Characters[8][0] = 'Í';
    	Characters[8][1] = 'I';
    	Characters[9][0] = 'Ó';
    	Characters[9][1] = 'O';
    	Characters[10][0] = 'Ú';
    	Characters[10][1] = 'U';
    	Characters[11][0] = 'Ñ';
    	Characters[11][1] = 'N';
    	Characters[12][0] = 'ç';
    	Characters[12][1] = 'c';
    	Characters[13][0] = 'Ç';
    	Characters[13][1] = 'C';
    }
    
    public static String toASCII(String text) throws Exception {
        for(int i = 0; i < Characters.length; i++) {
        	text = text.replaceAll(Character.toString(Characters[i][0]), Character.toString(Characters[i][1]));
        } 
        return new String(text);
    }
}
