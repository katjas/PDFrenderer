/**
 * ===========================================
 * Java Pdf Extraction Decoding Access Library
 * ===========================================
 * Project Info:  http://www.jpedal.org
 * (C) Copyright 1997-2011, IDRsolutions and Contributors.
 * 	This file is part of JPedal
 * Modified for use in PDF Renderer.
 *
 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.
 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 Lesser General Public License for more details.
 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 * ---------------
 * CCITT2D.java
 * ---------------
 */
package org.jpedal.io.filter.ccitt;

public class CCITT2D extends CCITT1D implements CCITTDecoder {

	int changingElemSize = 0;
	boolean is2D =true;

	public CCITT2D(byte[] rawData, int width, int height,
				   boolean blackIsOne, boolean isByteAligned) {

		super(rawData, width, height, blackIsOne, isByteAligned);
	}

	public byte[] decode() {

		decode2DRun();

		byte[] output=creatOutputFromBitset();

		//by default blackIs1 is false so black pixels do not need to be set
		// invert image if needed -
		if (!BlackIs1) {
			for (int i = 0; i < output.length; i++)
				output[i] = (byte) (255 - output[i]);
		}

		return output;
	}

	int bitOffset,currIndex;

	private void decode2DRun() {
		//setup cur and prev
		int[] prev = new int[width + 1];
		int[] curr = new int[width + 1];

		// Assume invisible preceding row of all white pixels and put
		// both black and white changing elements beyond the end of this image
		changingElemSize = 2;
		curr[0] = width;
		curr[1] = width;

		int byteReached = 0;

		int[] currentChangeElement = new int[2];

		for( int lines = 0;lines < height;lines++ ){


			if(isByteAligned && bitReached >0){

				//get bits over 8 and align to byte boundary
				int iPart = (bitReached)%8;
				int iDrop = 8-(iPart);
				if(iPart>0){
					bitReached = bitReached +iDrop;
				}
			}

			//swap cur and pre
			int[] temp = prev;
			prev = curr;
			curr = temp;

			// main loop to do whole line  (read required number of values for a line)
			set2D(prev, curr, changingElemSize, currentChangeElement);

			// Add the changing element beyond the current scanline for the other color too to stop errors
			if( curr.length != currIndex )
				curr[currIndex++] = bitOffset;

			// Number of changing elements in this scanline.
			changingElemSize = currIndex;
			byteReached = byteReached +scanlineStride;
		}
	}

	void set2D(int[] prev, int[] curr, int changingElemSize, int[] currentChangeElement) {

		// reset to defaults on each line
		isWhite = true;
		currIndex = 0;
		bitOffset = 0;

		int entry,code,bits=0,a0=-1;

		while( bitOffset < width){

			//read new value (does not always change both parts)
			getNextChangingElement( a0, isWhite, currentChangeElement, prev,changingElemSize );

			int bitCode=get1DBits(7);
			bitReached=bitReached+7;

			entry =(code2D[bitCode] & 255 );  //lookup next value

			// Get the code and the number of bits used up in command
			code = ( entry & 0x78 ) >>> 3;

			if(!is2D)
				bits = entry & 0x07;
			else if(code!=11) //and roll on if not vertical by set amount
				updatePointer( 7 - (entry & 7) );

			int pixelCount;

			switch(code){ //4 different code cases
				case 0:// // Pass

					pixelCount=currentChangeElement[1] - bitOffset;
					if( !isWhite ){ //fill in any bits
						out.set(outPtr,outPtr+pixelCount,true);
					}
					outPtr=outPtr+pixelCount; //update ptr
					bitOffset = currentChangeElement[1]; //and update pointers
					a0 = currentChangeElement[1]; //and update pointers

					// Set pointer to consume the correct number of bits.
					if(!is2D)
						bitReached=bitReached- (7 - bits);

					break;

				case 1: // Horizontal run of black/white or white/black

					if(!is2D)
						bitReached=bitReached- (7 - bits);

					//white then black run if isWhite or black/white
					//(we do not need to set white pixels, just ignore so routines slightly different)
					if( isWhite ){ //white then black
						pixelCount = getWhiteRunCodeWord();
						outPtr=outPtr+pixelCount;
						bitOffset += pixelCount;
						curr[currIndex++] = bitOffset;
						pixelCount = getBlackRunCodeWord();
						out.set(outPtr,outPtr+pixelCount,true);
						outPtr=outPtr+pixelCount;
					}else{ //  black run and then a white run after
						pixelCount = getBlackRunCodeWord();
						out.set(outPtr,outPtr+pixelCount,true);
						outPtr=outPtr+pixelCount;
						bitOffset += pixelCount;
						curr[currIndex++] = bitOffset;
						pixelCount = getWhiteRunCodeWord();
						outPtr=outPtr+pixelCount;
					}
					bitOffset = bitOffset + pixelCount;
					curr[currIndex++] = bitOffset;
					a0 = bitOffset;

					break;

				case 11: //other cases


					int nextValue=get1DBits(3);
					bitReached=bitReached+3;

					if( nextValue != 7 )
						throw new RuntimeException("Unexpected value "+nextValue);

					int zeroBits = 0;
					boolean isDone = false;
					while( !isDone ){
						while( true){

							bitCode=get1DBits(1);
							bitReached=bitReached+1;

							if(bitCode==1)
								break;

							zeroBits++;
						}

						if( zeroBits > 5 ){

							// Zeros before exit code
							zeroBits = zeroBits - 6;
							if( !isWhite && zeroBits > 0)
								curr[currIndex++] = bitOffset;

							// Zeros before the exit code
							bitOffset += zeroBits;
							if( zeroBits > 0 )
								isWhite = true;

							// Read in the bit which specifies the color of this run
							bitCode=get1DBits(1);
							bitReached=bitReached+1;

							if( bitCode == 0 ){
								if( !isWhite )
									curr[currIndex++] = bitOffset;
								isWhite = true;
							}else{
								if( isWhite )
									curr[currIndex++] = bitOffset;
								isWhite = false;
							}
							isDone = true;
						}
						if( zeroBits == 5 ){ //finishes on white
							if( !isWhite )
								curr[currIndex++] = bitOffset;
							bitOffset += zeroBits;

							isWhite = true;
						}else{  //finishes on black
							bitOffset += zeroBits;
							curr[currIndex++] = bitOffset;
							out.set(outPtr,outPtr+1,true);
							outPtr=outPtr+1;
							++bitOffset;

							isWhite = false;
						}
					}

					break;

				default:  // Vertical
					if( code > 8 )
						throw new RuntimeException("CCITT unexpected value");

					curr[currIndex++] = currentChangeElement[0] + ( code - 5 );

					// We write the current color till a1 - 1 pos, since a1 is where the next color starts
					pixelCount=currentChangeElement[0] + ( code - 5 ) - bitOffset;
					if( !isWhite ){
						out.set(outPtr,outPtr+pixelCount,true);
					}
					outPtr=outPtr+pixelCount;
					a0 = currentChangeElement[0] + ( code - 5 );
					bitOffset = a0;
					isWhite = !isWhite; //switch color

					if(!is2D)
						bitReached=bitReached- (7 - bits);

					break;
			}
		}
	}

	// Initial black run look up table, uses the first 4 bits of a code
	static final int[] initBlack ={3226, 6412, 200, 168, 38, 38, 134, 134, 100, 100, 100,100, 68, 68, 68, 68};

	private static final byte[] code2D =
			{
					80, 88, 23, 71, 30, 30, 62, 62, 4, 4, 4, 4, 4, 4, 4,
					4, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11,
					11, 11, 11, 11, 35, 35, 35, 35, 35, 35, 35, 35, 35,
					35, 35, 35, 35, 35, 35, 35, 51, 51, 51, 51, 51, 51,
					51, 51, 51, 51, 51, 51, 51, 51, 51, 51, 41, 41, 41,
					41, 41, 41, 41, 41, 41, 41, 41, 41, 41, 41, 41, 41,
					41, 41, 41, 41, 41, 41, 41, 41, 41, 41, 41, 41, 41,
					41, 41, 41, 41, 41, 41, 41, 41, 41, 41, 41, 41, 41,
					41, 41, 41, 41, 41, 41, 41, 41, 41, 41, 41, 41, 41,
					41, 41, 41, 41, 41, 41, 41, 41, 41,
			};

	// Main black run table, using the last 9 bits of possible 13 bit code
	static final int[] black =
			{
					62, 62, 30, 30, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
					0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 3225,
					3225, 3225, 3225, 3225, 3225, 3225, 3225, 3225, 3225,
					3225, 3225, 3225, 3225, 3225, 3225, 3225, 3225, 3225,
					3225, 3225, 3225, 3225, 3225, 3225, 3225, 3225, 3225,
					3225, 3225, 3225, 3225, 588, 588, 588, 588, 588, 588,
					588, 588, 1680, 1680, 20499, 22547, 24595, 26643, 1776,
					1776, 1808, 1808, -24557, -22509, -20461, -18413, 1904,
					1904, 1936, 1936, -16365, -14317, 782, 782, 782, 782,
					// 96 - 103
					814, 814, 814, 814, -12269, -10221, 10257, 10257, /* 104 - 111*/ 12305,
					12305, 14353, 14353, 16403, 18451, 1712, 1712, /* 112 - 119*/ 1744,
					1744, 28691, 30739, -32749, -30701, -28653, -26605,
					// 120 - 127
					2061, 2061, 2061, 2061, 2061, 2061, 2061, 2061, /* 128 - 135*/ 424,
					424, 424, 424, 424, 424, 424, 424, /* 136 - 143*/ 424, 424, 424, 424,
					424, 424, 424, 424, /* 144 - 151*/ 424, 424, 424, 424, 424, 424, 424,
					424, /* 152 - 159*/ 424, 424, 424, 424, 424, 424, 424, 424, /* 160 - 167*/ 750, 750,
					750, 750, 1616, 1616, 1648, 1648, /* 168 - 175*/ 1424, 1424, 1456,
					1456, 1488, 1488, 1520, 1520, /* 176 - 183*/ 1840, 1840, 1872, 1872,
					1968, 1968, 8209, 8209, /* 184 - 191*/ 524, 524, 524, 524, 524, 524,
					524, 524, 556, 556, 556, 556, 556, 556, 556, 556, 1552,
					1552, 1584, 1584, 2000, 2000, 2032, 2032, 976, 976,
					1008, 1008, 1040, 1040, 1072, 1072, 1296, 1296, 1328,
					1328, 718, 718, 718, 718, 456, 456, 456, 456, 456,
					456, 456, 456,  456, 456, 456, 456, 456, 456, 456, 456,
					456, 456, 456, 456, 456, 456, 456, 456, 456, 456, 456,
					456, 456, 456, 456, 456, 326, 326, 326, 326, 326, 326,
					326, 326, /* 264 - 271*/ 326, 326, 326, 326, 326, 326, 326, 326, 326,
					326, 326, 326, 326, 326, 326, 326, /* 280 - 287*/ 326, 326, 326, 326,
					326, 326, 326, 326, /* 288 - 295*/ 326, 326, 326, 326, 326, 326, 326,
					326, /* 296 - 303*/ 326, 326, 326, 326, 326, 326, 326, 326, /* 304 - 311*/ 326, 326,
					326, 326, 326, 326, 326, 326, /* 312 - 319*/ 326, 326, 326, 326, 326,
					326, 326, 326, /* 320 - 327*/ 358, 358, 358, 358, 358, 358, 358, 358,
					358, 358, 358, 358, 358, 358, 358, 358, 358, 358, 358,
					358, 358, 358, 358, 358, 358, 358, 358, 358, 358, 358,
					358, 358, 358, 358, 358, 358, 358, 358, 358, 358, /* 360 - 367*/ 358,
					358, 358, 358, 358, 358, 358, 358, /* 368 - 375*/ 358, 358, 358, 358,
					358, 358, 358, 358, 358, 358, 358, 358, 358, 358, 358,
					358, 490, 490, 490, 490, 490, 490, 490, 490, /* 392 - 399*/ 490, 490,
					490, 490, 490, 490, 490, 490, /* 400 - 407*/ 4113, 4113, 6161, 6161,
					848, 848, 880, 880, /* 408 - 415*/ 912, 912, 944, 944, 622, 622, 622,
					622, 654, 654, 654, 654, 1104, 1104, 1136, 1136, /* 424 - 431*/ 1168,
					1168, 1200, 1200, 1232, 1232, 1264, 1264, /* 432 - 439*/ 686, 686,
					686, 686, 1360, 1360, 1392, 1392, /* 440 - 447*/ 12, 12, 12, 12, 12,
					12, 12, 12, 390, 390, 390, 390, 390, 390, 390, 390,
					390, 390, 390, 390, 390, 390, 390, 390, 390, 390, 390,
					390, 390, 390, 390, 390, 390, 390, 390, 390, 390, 390,
					390, 390, 390, 390, 390, 390, 390, 390, 390, 390, 390,
					390, 390, 390, 390, 390, 390, 390, 390, 390, 390, 390,
					390, 390, 390, 390, 390, 390, 390, 390, 390, 390, 390,
					390,
			};

	// Additional make up codes for both White and Black runs
	static final int[] additionalMakeup ={28679, 28679, 31752, 32777, 33801, 34825,35849, 36873, 29703, 29703,30727, 30727, 37897, 38921,39945, 40969};

	// The main 10 bit white runs lookup table
	static final int[] white =
			{
					6430, 6400, 6400, 6400, 3225, 3225, 3225, 3225, 944,
					944, 944, 944, 976, 976, 976, 976, 1456, 1456, 1456,
					1456, 1488, 1488, 1488, 1488, 718, 718, 718, 718, 718,
					718, 718, 718, 750, 750, 750, 750, 750, 750, 750, 750,
					1520, 1520, 1520, 1520, 1552, 1552, 1552, 1552, /* 48 - 55*/ 428,
					428, 428, 428, 428, 428, 428, 428, /* 56 - 63*/ 428, 428, 428, 428,
					428, 428, 428, 428, /* 64 - 71*/ 654, 654, 654, 654, 654, 654, 654,
					654, /* 72 - 79*/ 1072, 1072, 1072, 1072, 1104, 1104, 1104, 1104,
					// 80 - 87
					1136, 1136, 1136, 1136, 1168, 1168, 1168, 1168, /* 88 - 95*/ 1200,
					1200, 1200, 1200, 1232, 1232, 1232, 1232, /* 96 - 103*/ 622, 622,
					622, 622, 622, 622, 622, 622, /* 104 - 111*/ 1008, 1008, 1008, 1008,
					1040, 1040, 1040, 1040, /* 112 - 119*/ 44, 44, 44, 44, 44, 44, 44,
					44, /* 120 - 127*/ 44, 44, 44, 44, 44, 44, 44, 44, /* 128 - 135*/ 396, 396, 396,
					396, 396, 396, 396, 396, /* 136 - 143*/ 396, 396, 396, 396, 396, 396,
					396, 396, /* 144 - 151*/ 1712, 1712, 1712, 1712, 1744, 1744, 1744,
					1744, /* 152 - 159*/ 846, 846, 846, 846, 846, 846, 846, 846, /* 160 - 167*/ 1264,
					1264, 1264, 1264, 1296, 1296, 1296, 1296, /* 168 - 175*/ 1328, 1328,
					1328, 1328, 1360, 1360, 1360, 1360, /* 176 - 183*/ 1392, 1392, 1392,
					1392, 1424, 1424, 1424, 1424, /* 184 - 191*/ 686, 686, 686, 686, 686,
					686, 686, 686, /* 192 - 199*/ 910, 910, 910, 910, 910, 910, 910, 910,
					// 200 - 207
					1968, 1968, 1968, 1968, 2000, 2000, 2000, 2000, /* 208 - 215*/ 2032,
					2032, 2032, 2032, 16, 16, 16, 16, /* 216 - 223*/ 10257, 10257, 10257,
					10257, 12305, 12305, 12305, 12305, /* 224 - 231*/ 330, 330, 330, 330,
					330, 330, 330, 330, /* 232 - 239*/ 330, 330, 330, 330, 330, 330, 330,
					330, /* 240 - 247*/ 330, 330, 330, 330, 330, 330, 330, 330, /* 248 - 255*/ 330, 330,
					330, 330, 330, 330, 330, 330, /* 256 - 263*/ 362, 362, 362, 362, 362,
					362, 362, 362, /* 264 - 271*/ 362, 362, 362, 362, 362, 362, 362, 362,
					// 272 - 279
					362, 362, 362, 362, 362, 362, 362, 362, /* 280 - 287*/ 362, 362, 362,
					362, 362, 362, 362, 362, /* 288 - 295*/ 878, 878, 878, 878, 878, 878,
					878, 878, /* 296 - 303*/ 1904, 1904, 1904, 1904, 1936, 1936, 1936,
					1936, /* 304 - 311*/ -18413, -18413, -16365, -16365, -14317, -14317,
					-10221, -10221, /* 312 - 319*/ 590, 590, 590, 590, 590, 590, 590,
					590, /* 320 - 327*/ 782, 782, 782, 782, 782, 782, 782, 782, /* 328 - 335*/ 1584,
					1584, 1584, 1584, 1616, 1616, 1616, 1616, /* 336 - 343*/ 1648, 1648,
					1648, 1648, 1680, 1680, 1680, 1680, /* 344 - 351*/ 814, 814, 814,
					814, 814, 814, 814, 814, /* 352 - 359*/ 1776, 1776, 1776, 1776, 1808,
					1808, 1808, 1808, /* 360 - 367*/ 1840, 1840, 1840, 1840, 1872, 1872,
					1872, 1872, /* 368 - 375*/ 6157, 6157, 6157, 6157, 6157, 6157, 6157,
					6157, /* 376 - 383*/ 6157, 6157, 6157, 6157, 6157, 6157, 6157, 6157,
					// 384 - 391
					-12275, -12275, -12275, -12275, -12275, -12275, -12275,
					-12275, /* 392 - 399*/ -12275, -12275, -12275, -12275, -12275, -12275,
					-12275, -12275, /* 400 - 407*/ 14353, 14353, 14353, 14353, 16401,
					16401, 16401, 16401, /* 408 - 415*/ 22547, 22547, 24595, 24595, 20497,
					20497, 20497, 20497, /* 416 - 423*/ 18449, 18449, 18449, 18449, 26643,
					26643, 28691, 28691, /* 424 - 431*/ 30739, 30739, -32749, -32749,
					-30701, -30701, -28653, -28653, /* 432 - 439*/ -26605, -26605, -24557,
					-24557, -22509, -22509, -20461, -20461, /* 440 - 447*/ 8207, 8207,
					8207, 8207, 8207, 8207, 8207, 8207, /* 448 - 455*/ 72, 72, 72, 72,
					72, 72, 72, 72, 72, 72, 72, 72, 72, 72, 72, 72, /* 464 - 471*/ 72,
					72, 72, 72, 72, 72, 72, 72, /* 472 - 479*/ 72, 72, 72, 72, 72, 72,
					72, 72, 72, 72, 72, 72, 72, 72, 72, 72, /* 488 - 495*/ 72, 72, 72,
					72, 72, 72, 72, 72, /* 496 - 503*/ 72, 72, 72, 72, 72, 72, 72, 72,
					72, 72, 72, 72, 72, 72, 72, 72, /* 512 - 519*/ 104, 104, 104, 104,
					104, 104, 104, 104, /* 520 - 527*/ 104, 104, 104, 104, 104, 104, 104,
					104, /* 528 - 535*/ 104, 104, 104, 104, 104, 104, 104, 104, /* 536 - 543*/ 104, 104,
					104, 104, 104, 104, 104, 104, /* 544 - 551*/ 104, 104, 104, 104, 104,
					104, 104, 104, /* 552 - 559*/ 104, 104, 104, 104, 104, 104, 104, 104,
					// 560 - 567
					104, 104, 104, 104, 104, 104, 104, 104, /* 568 - 575*/ 104, 104, 104,
					104, 104, 104, 104, 104, /* 576 - 583*/ 4107, 4107, 4107, 4107, 4107,
					4107, 4107, 4107, /* 584 - 591*/ 4107, 4107, 4107, 4107, 4107, 4107,
					4107, 4107, /* 592 - 599*/ 4107, 4107, 4107, 4107, 4107, 4107, 4107,
					4107, /* 600 - 607*/ 4107, 4107, 4107, 4107, 4107, 4107, 4107, 4107,
					// 608 - 615
					266, 266, 266, 266, 266, 266, 266, 266, /* 616 - 623*/ 266, 266, 266,
					266, 266, 266, 266, 266, /* 624 - 631*/ 266, 266, 266, 266, 266, 266,
					266, 266, /* 632 - 639*/ 266, 266, 266, 266, 266, 266, 266, 266, /* 640 - 647*/ 298,
					298, 298, 298, 298, 298, 298, 298, /* 648 - 655*/ 298, 298, 298, 298,
					298, 298, 298, 298, /* 656 - 663*/ 298, 298, 298, 298, 298, 298, 298,
					298, /* 664 - 671*/ 298, 298, 298, 298, 298, 298, 298, 298, /* 672 - 679*/ 524, 524,
					524, 524, 524, 524, 524, 524, /* 680 - 687*/ 524, 524, 524, 524, 524,
					524, 524, 524, /* 688 - 695*/ 556, 556, 556, 556, 556, 556, 556, 556,
					// 696 - 703
					556, 556, 556, 556, 556, 556, 556, 556, /* 704 - 711*/ 136, 136, 136,
					136, 136, 136, 136, 136, /* 712 - 719*/ 136, 136, 136, 136, 136, 136,
					136, 136, /* 720 - 727*/ 136, 136, 136, 136, 136, 136, 136, 136, /* 728 - 735*/ 136,
					136, 136, 136, 136, 136, 136, 136, /* 736 - 743*/ 136, 136, 136, 136,
					136, 136, 136, 136, /* 744 - 751*/ 136, 136, 136, 136, 136, 136, 136,
					136, /* 752 - 759*/ 136, 136, 136, 136, 136, 136, 136, 136, /* 760 - 767*/ 136, 136,
					136, 136, 136, 136, 136, 136, /* 768 - 775*/ 168, 168, 168, 168, 168,
					168, 168, 168, /* 776 - 783*/ 168, 168, 168, 168, 168, 168, 168, 168,
					// 784 - 791
					168, 168, 168, 168, 168, 168, 168, 168, /* 792 - 799*/ 168, 168, 168,
					168, 168, 168, 168, 168, /* 800 - 807*/ 168, 168, 168, 168, 168, 168,
					168, 168, /* 808 - 815*/ 168, 168, 168, 168, 168, 168, 168, 168, /* 816 - 823*/ 168,
					168, 168, 168, 168, 168, 168, 168, /* 824 - 831*/ 168, 168, 168, 168,
					168, 168, 168, 168, /* 832 - 839*/ 460, 460, 460, 460, 460, 460, 460,
					460, /* 840 - 847*/ 460, 460, 460, 460, 460, 460, 460, 460, /* 848 - 855*/ 492, 492,
					492, 492, 492, 492, 492, 492, /* 856 - 863*/ 492, 492, 492, 492, 492,
					492, 492, 492, /* 864 - 871*/ 2059, 2059, 2059, 2059, 2059, 2059,
					2059, 2059, /* 872 - 879*/ 2059, 2059, 2059, 2059, 2059, 2059, 2059,
					2059, /* 880 - 887*/ 2059, 2059, 2059, 2059, 2059, 2059, 2059, 2059,
					// 888 - 895
					2059, 2059, 2059, 2059, 2059, 2059, 2059, 2059, /* 896 - 903*/ 200,
					200, 200, 200, 200, 200, 200, 200, /* 904 - 911*/ 200, 200, 200, 200,
					200, 200, 200, 200, /* 912 - 919*/ 200, 200, 200, 200, 200, 200, 200,
					200, /* 920 - 927*/ 200, 200, 200, 200, 200, 200, 200, 200, /* 928 - 935*/ 200, 200,
					200, 200, 200, 200, 200, 200, /* 936 - 943*/ 200, 200, 200, 200, 200,
					200, 200, 200, /* 944 - 951*/ 200, 200, 200, 200, 200, 200, 200, 200,
					// 952 - 959
					200, 200, 200, 200, 200, 200, 200, 200, 232, 232, 232,
					232, 232, 232, 232, 232, 232, 232, 232, 232, 232, 232,
					232, 232, 232, 232, 232, 232, 232, 232, 232, 232, 232,
					232, 232, 232, 232, 232, 232, 232, 232, 232, 232, 232,
					232, 232, 232, 232, 232, 232, 232, 232, 232, 232, 232,
					232, 232, 232, 232, 232, 232, 232, 232, 232, 232, 232,
					232, 232, 232, 232, 232, 232,
			};

	static final int[] twoBitBlack ={292, 260, 226, 226};


	// Returns run length of black pixels
	int getBlackRunCodeWord(){

		int entry, bits, code, length =0;
		boolean isBlack = true;

		while( isBlack ){

			int bitCode=get1DBits(4);
			bitReached=bitReached+4;

			entry = initBlack[bitCode];

			// Get the fields from the entry
			bits = ( entry >>> 1 ) & 15;
			code = ( entry >>> 5 ) & 0x07ff;

			switch(code){
				case 100:

					bitCode=get1DBits(9);
					bitReached=bitReached+9;

					entry = black[bitCode];

					// Get the fields from the entry
					bits = ( entry >>> 1 ) & 0x000f;
					code = ( entry >>> 5 ) & 0x07ff;
					if( bits == 12 ){
						// Additional makeup codes
						updatePointer( 5 );

						bitCode=get1DBits(4);
						bitReached=bitReached+4;


						entry = additionalMakeup[bitCode];
						bits = ( entry >>> 1 ) & 0x07;
						code = ( entry >>> 4 ) & 0x0fff;
						length += code;
						updatePointer( 4 - bits );
					}else if( bits == 15 )
						throw new RuntimeException(( "CCITT unexpected EOL" ) );
					else {
						length += code;
						updatePointer( 9 - bits );
						if( (entry & 0x0001) == 0 )  //is Terminating
							isBlack = false;
					}
					break;

				case 200:

					bitCode=get1DBits(2);
					bitReached=bitReached+2;

					// Is a Terminating code
					entry = twoBitBlack[bitCode];
					code = ( entry >>> 5 ) & 0x07ff;
					length += code;
					bits = ( entry >>> 1 ) & 0x0f;
					updatePointer( 2 - bits );
					isBlack = false;

					break;

				default:

					// Is a Terminating code
					length += code;
					updatePointer( 4 - bits );
					isBlack = false;
					break;
			}
		}
		return length;
	}

	// Returns run length
	int getWhiteRunCodeWord(){

		int current, entry, bits, twoBits, code,length = 0;

		boolean isWhite = true;
		while( isWhite ){
			current = get1DBits(10);
			bitReached=bitReached+10;

			entry = white[current];

			// Get the  fields from the entry
			bits = ( entry >>> 1 ) & 0x0f;
			if( bits == 12 ){ // Additional Make up code
				// Get the next 2 bits

				twoBits=get1DBits(2);
				bitReached=bitReached+2;

				// Consolidate the 2 new bits and last 2 bits into 4 bits
				current = ( ( current << 2 ) & 0x000c ) | twoBits;
				entry = additionalMakeup[current];
				bits = ( entry >>> 1 ) & 7;
				code = ( entry >>> 4 ) & 0x0fff;
				length += code;
				updatePointer( 4 - bits );
			}else if( bits == 0 || bits == 15) {
				throw new RuntimeException("CCITT Error in getWhiteRunCodeWord");
			}else{
				code = ( entry >>> 5 ) & 0x07ff;
				length += code;
				updatePointer( 10 - bits );
				if((entry & 0x0001) == 0 )
					isWhite = false;
			}
		}
		return length;
	}

	private static void getNextChangingElement(int a0, boolean isWhite, int[] ret, int[] prevChangingElems, int changingElemSize){

		// If the previous match was at an odd element, we still
		// have to search the preceeding element.
		int start = 0;

		if( isWhite )
			start &= ~0x1; // Search even numbered elements
		else
			start |= 0x1; // Search odd numbered elements

		int i = start;
		for( ;i < changingElemSize;i += 2 ){
			int temp = prevChangingElems[i];
			if( temp > a0 )
			{
				ret[0] = temp;
				break;
			}
		}
		if( i + 1 < changingElemSize )
			ret[1] = prevChangingElems[i + 1];

	}

	// Move pointer backwards by given amount of bits
	private void updatePointer( int bitsToMoveBack )
	{

		bitReached=bitReached-bitsToMoveBack;

	}
}