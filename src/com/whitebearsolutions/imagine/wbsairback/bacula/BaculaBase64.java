package com.whitebearsolutions.imagine.wbsairback.bacula;

import com.whitebearsolutions.imagine.wbsairback.configuration.WBSAirbackConfiguration;
import com.whitebearsolutions.util.Command;


public class BaculaBase64 {

	private static char alphabet[] = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/=".toCharArray();
	private static byte codes[];

	static {
		codes = new byte[256];
		for(int i = 0; i < 256; i++) {
			codes[i] = -1;
		}

		for(int i = 65; i <= 90; i++) {
			codes[i] = (byte)(i - 65);
		}

		for(int i = 97; i <= 122; i++) {
			codes[i] = (byte)((26 + i) - 97);
		}

		for(int i = 48; i <= 57; i++) {
			codes[i] = (byte)((52 + i) - 48);
		}

		codes[43] = 62;
		codes[47] = 63;
	}
	
	public static long decode (String datas)	{
		try {
			String output = Command.systemCommand(WBSAirbackConfiguration.getBinBaculaBase64Decoder()+" "+datas);
			output.replace("\n", "");
			output.replace("\r", "");
			return Long.parseLong(output.trim());
		} catch (Exception ex) {
			char[] data = datas.toCharArray();
			long value = 0;
			for(int i=0;i<data.length;i++) {
				for(int j=0; j < alphabet.length;j++) {
					if(data[i] == alphabet[j]) {
						value = value + (j * (new Double(Math.pow(64,(data.length-i-1)))).intValue());
						break;
					}
				}
			}
			return value;
		}
	}
}
