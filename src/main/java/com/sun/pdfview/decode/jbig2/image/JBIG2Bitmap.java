/**
 * ===========================================
 * Java Pdf Extraction Decoding Access Library
 * ===========================================
 *
 * Project Info:  http://www.jpedal.org
 * (C) Copyright 1997-2008, IDRsolutions and Contributors.
 * Main Developer: Simon Barnett
 *
 * 	This file is part of JPedal
 *
 * Copyright (c) 2008, IDRsolutions
 * Copyright (c) 2011, Boris von Loesch 
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the IDRsolutions nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY IDRsolutions ``AS IS'' AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL IDRsolutions BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * Other JBIG2 image decoding implementations include
 * jbig2dec (http://jbig2dec.sourceforge.net/)
 * xpdf (http://www.foolabs.com/xpdf/)
 * 
 * The final draft JBIG2 specification can be found at http://www.jpeg.org/public/fcd14492.pdf
 * 
 * All three of the above resources were used in the writing of this software, with methodologies,
 * processes and inspiration taken from all three.
 *
 * ---------------
 * JBIG2Bitmap.java
 * ---------------
 */
package com.sun.pdfview.decode.jbig2.image;

import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.io.IOException;

import com.sun.pdfview.decode.jbig2.JBIG2Exception;
import com.sun.pdfview.decode.jbig2.decoders.ArithmeticDecoder;
import com.sun.pdfview.decode.jbig2.decoders.DecodeIntResult;
import com.sun.pdfview.decode.jbig2.decoders.HuffmanDecoder;
import com.sun.pdfview.decode.jbig2.decoders.JBIG2StreamDecoder;
import com.sun.pdfview.decode.jbig2.decoders.MMRDecoder;
import com.sun.pdfview.decode.jbig2.util.BinaryOperation;

public final class JBIG2Bitmap {

	private int width, height, line;
	private int bitmapNumber;
	public FastBitSet data;

	//private BitSet data;
	
	//private static int counter = 0;
	
	private ArithmeticDecoder arithmeticDecoder;
	private HuffmanDecoder huffmanDecoder;
	private MMRDecoder mmrDecoder;
	
	public JBIG2Bitmap(int width, int height, ArithmeticDecoder arithmeticDecoder, HuffmanDecoder huffmanDecoder, MMRDecoder mmrDecoder) {
		this.width = width;
		this.height = height;
		this.arithmeticDecoder = arithmeticDecoder;
		this.huffmanDecoder = huffmanDecoder;
		this.mmrDecoder = mmrDecoder;
		
		this.line = (width + 7) >> 3;

		this.data = new FastBitSet(width * height);
	}

	public void readBitmap(boolean useMMR, int template, boolean typicalPredictionGenericDecodingOn, boolean useSkip, JBIG2Bitmap skipBitmap, short[] adaptiveTemplateX, short[] adaptiveTemplateY, int mmrDataLength) throws IOException, JBIG2Exception {

		if (useMMR) {

			//MMRDecoder mmrDecoder = MMRDecoder.getInstance();
			mmrDecoder.reset();

			int[] referenceLine = new int[width + 2];
			int[] codingLine = new int[width + 2];
			codingLine[0] = codingLine[1] = width;

			for (int row = 0; row < height; row++) {

				int i = 0;
				for (; codingLine[i] < width; i++) {
					referenceLine[i] = codingLine[i];
				}
				referenceLine[i] = referenceLine[i + 1] = width;

				int referenceI = 0;
				int codingI = 0;
				int a0 = 0;

				do {
					int code1 = mmrDecoder.get2DCode(), code2, code3;

					switch (code1) {
					case MMRDecoder.twoDimensionalPass:
						if (referenceLine[referenceI] < width) {
							a0 = referenceLine[referenceI + 1];
							referenceI += 2;
						}
						break;
					case MMRDecoder.twoDimensionalHorizontal:
						if ((codingI & 1) != 0) {
							code1 = 0;
							do {
								code1 += code3 = mmrDecoder.getBlackCode();
							} while (code3 >= 64);

							code2 = 0;
							do {
								code2 += code3 = mmrDecoder.getWhiteCode();
							} while (code3 >= 64);
						} else {
							code1 = 0;
							do {
								code1 += code3 = mmrDecoder.getWhiteCode();
							} while (code3 >= 64);

							code2 = 0;
							do {
								code2 += code3 = mmrDecoder.getBlackCode();
							} while (code3 >= 64);

						}
						if (code1 > 0 || code2 > 0) {
							a0 = codingLine[codingI++] = a0 + code1;
							a0 = codingLine[codingI++] = a0 + code2;

							while (referenceLine[referenceI] <= a0 && referenceLine[referenceI] < width) {
								referenceI += 2;
							}
						}
						break;
					case MMRDecoder.twoDimensionalVertical0:
						a0 = codingLine[codingI++] = referenceLine[referenceI];
						if (referenceLine[referenceI] < width) {
							referenceI++;
						}

						break;
					case MMRDecoder.twoDimensionalVerticalR1:
						a0 = codingLine[codingI++] = referenceLine[referenceI] + 1;
						if (referenceLine[referenceI] < width) {
							referenceI++;
							while (referenceLine[referenceI] <= a0 && referenceLine[referenceI] < width) {
								referenceI += 2;
							}
						}

						break;
					case MMRDecoder.twoDimensionalVerticalR2:
						a0 = codingLine[codingI++] = referenceLine[referenceI] + 2;
						if (referenceLine[referenceI] < width) {
							referenceI++;
							while (referenceLine[referenceI] <= a0 && referenceLine[referenceI] < width) {
								referenceI += 2;
							}
						}
						
						break;
					case MMRDecoder.twoDimensionalVerticalR3:
						a0 = codingLine[codingI++] = referenceLine[referenceI] + 3;
						if (referenceLine[referenceI] < width) {
							referenceI++;
							while (referenceLine[referenceI] <= a0 && referenceLine[referenceI] < width) {
								referenceI += 2;
							}
						}

						break;
					case MMRDecoder.twoDimensionalVerticalL1:
						a0 = codingLine[codingI++] = referenceLine[referenceI] - 1;
						if (referenceI > 0) {
							referenceI--;
						} else {
							referenceI++;
						}

						while (referenceLine[referenceI] <= a0 && referenceLine[referenceI] < width) {
							referenceI += 2;
						}

						break;
					case MMRDecoder.twoDimensionalVerticalL2:
						a0 = codingLine[codingI++] = referenceLine[referenceI] - 2;
						if (referenceI > 0) {
							referenceI--;
						} else {
							referenceI++;
						}

						while (referenceLine[referenceI] <= a0 && referenceLine[referenceI] < width) {
							referenceI += 2;
						}

						break;
					case MMRDecoder.twoDimensionalVerticalL3:
						a0 = codingLine[codingI++] = referenceLine[referenceI] - 3;
						if (referenceI > 0) {
							referenceI--;
						} else {
							referenceI++;
						}

						while (referenceLine[referenceI] <= a0 && referenceLine[referenceI] < width) {
							referenceI += 2;
						}

						break;
					default:
						if (JBIG2StreamDecoder.debug)
							System.out.println("Illegal code in JBIG2 MMR bitmap data");

						break;
					}
				} while (a0 < width);

				codingLine[codingI++] = width;

				for (int j = 0; codingLine[j] < width; j += 2) {
					for (int col = codingLine[j]; col < codingLine[j + 1]; col++) {
						setPixel(col, row, 1);
					}
				}
			}

			if (mmrDataLength >= 0) {
				mmrDecoder.skipTo(mmrDataLength);
			} else {
				if (mmrDecoder.get24Bits() != 0x001001) {
					if (JBIG2StreamDecoder.debug)
						System.out.println("Missing EOFB in JBIG2 MMR bitmap data");
				}
			}

		} else {

			//ArithmeticDecoder arithmeticDecoder = ArithmeticDecoder.getInstance();

			BitmapPointer cxPtr0 = new BitmapPointer(this), cxPtr1 = new BitmapPointer(this);
			BitmapPointer atPtr0 = new BitmapPointer(this), atPtr1 = new BitmapPointer(this), atPtr2 = new BitmapPointer(this), atPtr3 = new BitmapPointer(this);

			long ltpCX = 0;
			if (typicalPredictionGenericDecodingOn) {
				switch (template) {
				case 0:
					ltpCX = 0x3953;
					break;
				case 1:
					ltpCX = 0x079a;
					break;
				case 2:
					ltpCX = 0x0e3;
					break;
				case 3:
					ltpCX = 0x18a;
					break;
				}
			}

			boolean ltp = false;
			long cx, cx0, cx1, cx2;

			for (int row = 0; row < height; row++) {
				if (typicalPredictionGenericDecodingOn) {
					int bit = arithmeticDecoder.decodeBit(ltpCX, arithmeticDecoder.genericRegionStats);
					if (bit != 0) {
						ltp = !ltp;
					}

					if (ltp) {
						duplicateRow(row, row - 1);
						continue;
					}
				}

				int pixel;

				switch (template) {
				case 0:

					cxPtr0.setPointer(0, row - 2);
					cx0 = (cxPtr0.nextPixel() << 1); 
					cx0 |= cxPtr0.nextPixel();
					//cx0 = (BinaryOperation.bit32ShiftL(cx0, 1)) | cxPtr0.nextPixel();

					cxPtr1.setPointer(0, row - 1);
					cx1 = (cxPtr1.nextPixel() << 2); 
					cx1 |= (cxPtr1.nextPixel() << 1); 
					cx1 |= (cxPtr1.nextPixel());

					//cx1 = (BinaryOperation.bit32ShiftL(cx1, 1)) | cxPtr1.nextPixel();
					//cx1 = (BinaryOperation.bit32ShiftL(cx1, 1)) | cxPtr1.nextPixel();

					cx2 = 0;

					atPtr0.setPointer(adaptiveTemplateX[0], row + adaptiveTemplateY[0]);
					atPtr1.setPointer(adaptiveTemplateX[1], row + adaptiveTemplateY[1]);
					atPtr2.setPointer(adaptiveTemplateX[2], row + adaptiveTemplateY[2]);
					atPtr3.setPointer(adaptiveTemplateX[3], row + adaptiveTemplateY[3]);

					for (int col = 0; col < width; col++) {

						cx = (BinaryOperation.bit32ShiftL(cx0, 13)) | (BinaryOperation.bit32ShiftL(cx1, 8)) | (BinaryOperation.bit32ShiftL(cx2, 4)) | (atPtr0.nextPixel() << 3) | (atPtr1.nextPixel() << 2) | (atPtr2.nextPixel() << 1) | atPtr3.nextPixel();

						if (useSkip && skipBitmap.getPixel(col, row) != 0) {
							pixel = 0;
						} else {
							pixel = arithmeticDecoder.decodeBit(cx, arithmeticDecoder.genericRegionStats);
							if (pixel != 0) {
								data.set(row*width + col);
							}
						}

						cx0 = ((BinaryOperation.bit32ShiftL(cx0, 1)) | cxPtr0.nextPixel()) & 0x07;
						cx1 = ((BinaryOperation.bit32ShiftL(cx1, 1)) | cxPtr1.nextPixel()) & 0x1f;
						cx2 = ((BinaryOperation.bit32ShiftL(cx2, 1)) | pixel) & 0x0f;
					}
					break;

				case 1:

					cxPtr0.setPointer(0, row - 2);
					cx0 = (cxPtr0.nextPixel() << 2);
					cx0 |= (cxPtr0.nextPixel() << 1);
					cx0 |= (cxPtr0.nextPixel());
					/*cx0 = cxPtr0.nextPixel();
					cx0 = (BinaryOperation.bit32ShiftL(cx0, 1)) | cxPtr0.nextPixel();
					cx0 = (BinaryOperation.bit32ShiftL(cx0, 1)) | cxPtr0.nextPixel();*/

					cxPtr1.setPointer(0, row - 1);
					cx1 = (cxPtr1.nextPixel() << 2);
					cx1 |= (cxPtr1.nextPixel() << 1);
					cx1 |= (cxPtr1.nextPixel());
					/*cx1 = cxPtr1.nextPixel();
					cx1 = (BinaryOperation.bit32ShiftL(cx1, 1)) | cxPtr1.nextPixel();
					cx1 = (BinaryOperation.bit32ShiftL(cx1, 1)) | cxPtr1.nextPixel();*/

					cx2 = 0;

					atPtr0.setPointer(adaptiveTemplateX[0], row + adaptiveTemplateY[0]);

					for (int col = 0; col < width; col++) {

						cx = (BinaryOperation.bit32ShiftL(cx0, 9)) | (BinaryOperation.bit32ShiftL(cx1, 4)) | (BinaryOperation.bit32ShiftL(cx2, 1)) | atPtr0.nextPixel();

						if (useSkip && skipBitmap.getPixel(col, row) != 0) {
							pixel = 0;
						} else {
							pixel = arithmeticDecoder.decodeBit(cx, arithmeticDecoder.genericRegionStats);
							if (pixel != 0) {
								data.set(row*width + col);
							}
						}

						cx0 = ((BinaryOperation.bit32ShiftL(cx0, 1)) | cxPtr0.nextPixel()) & 0x0f;
						cx1 = ((BinaryOperation.bit32ShiftL(cx1, 1)) | cxPtr1.nextPixel()) & 0x1f;
						cx2 = ((BinaryOperation.bit32ShiftL(cx2, 1)) | pixel) & 0x07;
					}
					break;

				case 2:

					cxPtr0.setPointer(0, row - 2);
					cx0 = (cxPtr0.nextPixel() << 1); 
					cx0 |= (cxPtr0.nextPixel());
					/*cx0 = cxPtr0.nextPixel();
					cx0 = (BinaryOperation.bit32ShiftL(cx0, 1)) | cxPtr0.nextPixel();
					*/

					cxPtr1.setPointer(0, row - 1);
					cx1 = (cxPtr1.nextPixel() << 1); 
					cx1 |= (cxPtr1.nextPixel());
					/*cx1 = cxPtr1.nextPixel();
					cx1 = (BinaryOperation.bit32ShiftL(cx1, 1)) | cxPtr1.nextPixel();*/

					cx2 = 0;

					atPtr0.setPointer(adaptiveTemplateX[0], row + adaptiveTemplateY[0]);

					for (int col = 0; col < width; col++) {

						cx = (BinaryOperation.bit32ShiftL(cx0, 7)) | (BinaryOperation.bit32ShiftL(cx1, 3)) | (BinaryOperation.bit32ShiftL(cx2, 1)) | atPtr0.nextPixel();

						if (useSkip && skipBitmap.getPixel(col, row) != 0) {
							pixel = 0;
						} else {
							pixel = arithmeticDecoder.decodeBit(cx, arithmeticDecoder.genericRegionStats);
							if (pixel != 0) {
								data.set(row*width + col);
							}
						}

						cx0 = ((BinaryOperation.bit32ShiftL(cx0, 1)) | cxPtr0.nextPixel()) & 0x07;
						cx1 = ((BinaryOperation.bit32ShiftL(cx1, 1)) | cxPtr1.nextPixel()) & 0x0f;
						cx2 = ((BinaryOperation.bit32ShiftL(cx2, 1)) | pixel) & 0x03;
					}
					break;

				case 3:

					cxPtr1.setPointer(0, row - 1);
					cx1 = (cxPtr1.nextPixel() << 1);
					cx1 |= (cxPtr1.nextPixel());
					/*cx1 = cxPtr1.nextPixel();
					cx1 = (BinaryOperation.bit32ShiftL(cx1, 1)) | cxPtr1.nextPixel();
*/
					cx2 = 0;

					atPtr0.setPointer(adaptiveTemplateX[0], row + adaptiveTemplateY[0]);

					for (int col = 0; col < width; col++) {

						cx = (BinaryOperation.bit32ShiftL(cx1, 5)) | (BinaryOperation.bit32ShiftL(cx2, 1)) | atPtr0.nextPixel();

						if (useSkip && skipBitmap.getPixel(col, row) != 0) {
							pixel = 0;

						} else {
							pixel = arithmeticDecoder.decodeBit(cx, arithmeticDecoder.genericRegionStats);
							if (pixel != 0) {
								data.set(row*width + col);
							}
						}

						cx1 = ((BinaryOperation.bit32ShiftL(cx1, 1)) | cxPtr1.nextPixel()) & 0x1f;
						cx2 = ((BinaryOperation.bit32ShiftL(cx2, 1)) | pixel) & 0x0f;
					}
					break;
				}
			}
		}
	}

	public void readGenericRefinementRegion(int template, boolean typicalPredictionGenericRefinementOn, JBIG2Bitmap referredToBitmap, int referenceDX, int referenceDY, short[] adaptiveTemplateX, short[] adaptiveTemplateY) throws IOException, JBIG2Exception {

		//ArithmeticDecoder arithmeticDecoder = ArithmeticDecoder.getInstance();

		BitmapPointer cxPtr0, cxPtr1, cxPtr2, cxPtr3, cxPtr4, cxPtr5, cxPtr6, typicalPredictionGenericRefinementCXPtr0, typicalPredictionGenericRefinementCXPtr1, typicalPredictionGenericRefinementCXPtr2;

		long ltpCX;
		if (template != 0) {
			ltpCX = 0x008;

			cxPtr0 = new BitmapPointer(this);
			cxPtr1 = new BitmapPointer(this);
			cxPtr2 = new BitmapPointer(referredToBitmap);
			cxPtr3 = new BitmapPointer(referredToBitmap);
			cxPtr4 = new BitmapPointer(referredToBitmap);
			cxPtr5 = new BitmapPointer(this);
			cxPtr6 = new BitmapPointer(this);
			typicalPredictionGenericRefinementCXPtr0 = new BitmapPointer(referredToBitmap);
			typicalPredictionGenericRefinementCXPtr1 = new BitmapPointer(referredToBitmap);
			typicalPredictionGenericRefinementCXPtr2 = new BitmapPointer(referredToBitmap);
		} else {
			ltpCX = 0x0010;

			cxPtr0 = new BitmapPointer(this);
			cxPtr1 = new BitmapPointer(this);
			cxPtr2 = new BitmapPointer(referredToBitmap);
			cxPtr3 = new BitmapPointer(referredToBitmap);
			cxPtr4 = new BitmapPointer(referredToBitmap);
			cxPtr5 = new BitmapPointer(this);
			cxPtr6 = new BitmapPointer(referredToBitmap);
			typicalPredictionGenericRefinementCXPtr0 = new BitmapPointer(referredToBitmap);
			typicalPredictionGenericRefinementCXPtr1 = new BitmapPointer(referredToBitmap);
			typicalPredictionGenericRefinementCXPtr2 = new BitmapPointer(referredToBitmap);
		}

		long cx, cx0, cx2, cx3, cx4;
		long typicalPredictionGenericRefinementCX0, typicalPredictionGenericRefinementCX1, typicalPredictionGenericRefinementCX2;
		boolean ltp = false;

		for (int row = 0; row < height; row++) {

			if (template != 0) {

				cxPtr0.setPointer(0, row - 1);
				cx0 = cxPtr0.nextPixel();

				cxPtr1.setPointer(-1, row);

				cxPtr2.setPointer(-referenceDX, row - 1 - referenceDY);

				cxPtr3.setPointer(-1 - referenceDX, row - referenceDY);
				cx3 = cxPtr3.nextPixel();
				cx3 = (BinaryOperation.bit32ShiftL(cx3, 1)) | cxPtr3.nextPixel();

				cxPtr4.setPointer(-referenceDX, row + 1 - referenceDY);
				cx4 = cxPtr4.nextPixel();

				typicalPredictionGenericRefinementCX0 = typicalPredictionGenericRefinementCX1 = typicalPredictionGenericRefinementCX2 = 0;

				if (typicalPredictionGenericRefinementOn) {
					typicalPredictionGenericRefinementCXPtr0.setPointer(-1 - referenceDX, row - 1 - referenceDY);
					typicalPredictionGenericRefinementCX0 = typicalPredictionGenericRefinementCXPtr0.nextPixel();
					typicalPredictionGenericRefinementCX0 = (BinaryOperation.bit32ShiftL(typicalPredictionGenericRefinementCX0, 1)) | typicalPredictionGenericRefinementCXPtr0.nextPixel();
					typicalPredictionGenericRefinementCX0 = (BinaryOperation.bit32ShiftL(typicalPredictionGenericRefinementCX0, 1)) | typicalPredictionGenericRefinementCXPtr0.nextPixel();

					typicalPredictionGenericRefinementCXPtr1.setPointer(-1 - referenceDX, row - referenceDY);
					typicalPredictionGenericRefinementCX1 = typicalPredictionGenericRefinementCXPtr1.nextPixel();
					typicalPredictionGenericRefinementCX1 = (BinaryOperation.bit32ShiftL(typicalPredictionGenericRefinementCX1, 1)) | typicalPredictionGenericRefinementCXPtr1.nextPixel();
					typicalPredictionGenericRefinementCX1 = (BinaryOperation.bit32ShiftL(typicalPredictionGenericRefinementCX1, 1)) | typicalPredictionGenericRefinementCXPtr1.nextPixel();

					typicalPredictionGenericRefinementCXPtr2.setPointer(-1 - referenceDX, row + 1 - referenceDY);
					typicalPredictionGenericRefinementCX2 = typicalPredictionGenericRefinementCXPtr2.nextPixel();
					typicalPredictionGenericRefinementCX2 = (BinaryOperation.bit32ShiftL(typicalPredictionGenericRefinementCX2, 1)) | typicalPredictionGenericRefinementCXPtr2.nextPixel();
					typicalPredictionGenericRefinementCX2 = (BinaryOperation.bit32ShiftL(typicalPredictionGenericRefinementCX2, 1)) | typicalPredictionGenericRefinementCXPtr2.nextPixel();
				}

				for (int col = 0; col < width; col++) {

					cx0 = ((BinaryOperation.bit32ShiftL(cx0, 1)) | cxPtr0.nextPixel()) & 7;
					cx3 = ((BinaryOperation.bit32ShiftL(cx3, 1)) | cxPtr3.nextPixel()) & 7;
					cx4 = ((BinaryOperation.bit32ShiftL(cx4, 1)) | cxPtr4.nextPixel()) & 3;

					if (typicalPredictionGenericRefinementOn) {
						typicalPredictionGenericRefinementCX0 = ((BinaryOperation.bit32ShiftL(typicalPredictionGenericRefinementCX0, 1)) | typicalPredictionGenericRefinementCXPtr0.nextPixel()) & 7;
						typicalPredictionGenericRefinementCX1 = ((BinaryOperation.bit32ShiftL(typicalPredictionGenericRefinementCX1, 1)) | typicalPredictionGenericRefinementCXPtr1.nextPixel()) & 7;
						typicalPredictionGenericRefinementCX2 = ((BinaryOperation.bit32ShiftL(typicalPredictionGenericRefinementCX2, 1)) | typicalPredictionGenericRefinementCXPtr2.nextPixel()) & 7;

						int decodeBit = arithmeticDecoder.decodeBit(ltpCX, arithmeticDecoder.refinementRegionStats);
						if (decodeBit != 0) {
							ltp = !ltp;
						}
						if (typicalPredictionGenericRefinementCX0 == 0 && typicalPredictionGenericRefinementCX1 == 0 && typicalPredictionGenericRefinementCX2 == 0) {
							setPixel(col, row, 0);
							continue;
						} else if (typicalPredictionGenericRefinementCX0 == 7 && typicalPredictionGenericRefinementCX1 == 7 && typicalPredictionGenericRefinementCX2 == 7) {
							setPixel(col, row, 1);
							continue;
						}
					}

					cx = (BinaryOperation.bit32ShiftL(cx0, 7)) | (cxPtr1.nextPixel() << 6) | (cxPtr2.nextPixel() << 5) | (BinaryOperation.bit32ShiftL(cx3, 2)) | cx4;

					int pixel = arithmeticDecoder.decodeBit(cx, arithmeticDecoder.refinementRegionStats);
					if (pixel == 1) {
						data.set(row*width + col);
					}
				}

			} else {

				cxPtr0.setPointer(0, row - 1);
				cx0 = cxPtr0.nextPixel();

				cxPtr1.setPointer(-1, row);

				cxPtr2.setPointer(-referenceDX, row - 1 - referenceDY);
				cx2 = cxPtr2.nextPixel();

				cxPtr3.setPointer(-1 - referenceDX, row - referenceDY);
				cx3 = cxPtr3.nextPixel();
				cx3 = (BinaryOperation.bit32ShiftL(cx3, 1)) | cxPtr3.nextPixel();

				cxPtr4.setPointer(-1 - referenceDX, row + 1 - referenceDY);
				cx4 = cxPtr4.nextPixel();
				cx4 = (BinaryOperation.bit32ShiftL(cx4, 1)) | cxPtr4.nextPixel();

				cxPtr5.setPointer(adaptiveTemplateX[0], row + adaptiveTemplateY[0]);

				cxPtr6.setPointer(adaptiveTemplateX[1] - referenceDX, row + adaptiveTemplateY[1] - referenceDY);

				typicalPredictionGenericRefinementCX0 = typicalPredictionGenericRefinementCX1 = typicalPredictionGenericRefinementCX2 = 0;
				if (typicalPredictionGenericRefinementOn) {
					typicalPredictionGenericRefinementCXPtr0.setPointer(-1 - referenceDX, row - 1 - referenceDY);
					typicalPredictionGenericRefinementCX0 = typicalPredictionGenericRefinementCXPtr0.nextPixel();
					typicalPredictionGenericRefinementCX0 = (BinaryOperation.bit32ShiftL(typicalPredictionGenericRefinementCX0, 1)) | typicalPredictionGenericRefinementCXPtr0.nextPixel();
					typicalPredictionGenericRefinementCX0 = (BinaryOperation.bit32ShiftL(typicalPredictionGenericRefinementCX0, 1)) | typicalPredictionGenericRefinementCXPtr0.nextPixel();

					typicalPredictionGenericRefinementCXPtr1.setPointer(-1 - referenceDX, row - referenceDY);
					typicalPredictionGenericRefinementCX1 = typicalPredictionGenericRefinementCXPtr1.nextPixel();
					typicalPredictionGenericRefinementCX1 = (BinaryOperation.bit32ShiftL(typicalPredictionGenericRefinementCX1, 1)) | typicalPredictionGenericRefinementCXPtr1.nextPixel();
					typicalPredictionGenericRefinementCX1 = (BinaryOperation.bit32ShiftL(typicalPredictionGenericRefinementCX1, 1)) | typicalPredictionGenericRefinementCXPtr1.nextPixel();

					typicalPredictionGenericRefinementCXPtr2.setPointer(-1 - referenceDX, row + 1 - referenceDY);
					typicalPredictionGenericRefinementCX2 = typicalPredictionGenericRefinementCXPtr2.nextPixel();
					typicalPredictionGenericRefinementCX2 = (BinaryOperation.bit32ShiftL(typicalPredictionGenericRefinementCX2, 1)) | typicalPredictionGenericRefinementCXPtr2.nextPixel();
					typicalPredictionGenericRefinementCX2 = (BinaryOperation.bit32ShiftL(typicalPredictionGenericRefinementCX2, 1)) | typicalPredictionGenericRefinementCXPtr2.nextPixel();
				}

				for (int col = 0; col < width; col++) {

					cx0 = ((BinaryOperation.bit32ShiftL(cx0, 1)) | cxPtr0.nextPixel()) & 3;
					cx2 = ((BinaryOperation.bit32ShiftL(cx2, 1)) | cxPtr2.nextPixel()) & 3;
					cx3 = ((BinaryOperation.bit32ShiftL(cx3, 1)) | cxPtr3.nextPixel()) & 7;
					cx4 = ((BinaryOperation.bit32ShiftL(cx4, 1)) | cxPtr4.nextPixel()) & 7;

					if (typicalPredictionGenericRefinementOn) {
						typicalPredictionGenericRefinementCX0 = ((BinaryOperation.bit32ShiftL(typicalPredictionGenericRefinementCX0, 1)) | typicalPredictionGenericRefinementCXPtr0.nextPixel()) & 7;
						typicalPredictionGenericRefinementCX1 = ((BinaryOperation.bit32ShiftL(typicalPredictionGenericRefinementCX1, 1)) | typicalPredictionGenericRefinementCXPtr1.nextPixel()) & 7;
						typicalPredictionGenericRefinementCX2 = ((BinaryOperation.bit32ShiftL(typicalPredictionGenericRefinementCX2, 1)) | typicalPredictionGenericRefinementCXPtr2.nextPixel()) & 7;

						int decodeBit = arithmeticDecoder.decodeBit(ltpCX, arithmeticDecoder.refinementRegionStats);
						if (decodeBit == 1) {
							ltp = !ltp;
						}
						if (typicalPredictionGenericRefinementCX0 == 0 && typicalPredictionGenericRefinementCX1 == 0 && typicalPredictionGenericRefinementCX2 == 0) {
							setPixel(col, row, 0);
							continue;
						} else if (typicalPredictionGenericRefinementCX0 == 7 && typicalPredictionGenericRefinementCX1 == 7 && typicalPredictionGenericRefinementCX2 == 7) {
							setPixel(col, row, 1);
							continue;
						}
					}

					cx = (BinaryOperation.bit32ShiftL(cx0, 11)) | (cxPtr1.nextPixel() << 10) | (BinaryOperation.bit32ShiftL(cx2, 8)) | (BinaryOperation.bit32ShiftL(cx3, 5)) | (BinaryOperation.bit32ShiftL(cx4, 2)) | (cxPtr5.nextPixel() << 1) | cxPtr6.nextPixel();

					int pixel = arithmeticDecoder.decodeBit(cx, arithmeticDecoder.refinementRegionStats);
					if (pixel == 1) {
						setPixel(col, row, 1);
					}
				}
			}
		}
	}

	public void readTextRegion(boolean huffman, boolean symbolRefine, int noOfSymbolInstances, int logStrips, int noOfSymbols, int[][] symbolCodeTable, int symbolCodeLength, JBIG2Bitmap[] symbols, int defaultPixel, int combinationOperator, boolean transposed, int referenceCorner, int sOffset, int[][] huffmanFSTable, int[][] huffmanDSTable, int[][] huffmanDTTable, int[][] huffmanRDWTable, int[][] huffmanRDHTable, int[][] huffmanRDXTable, int[][] huffmanRDYTable, int[][] huffmanRSizeTable, int template, short[] symbolRegionAdaptiveTemplateX,
			short[] symbolRegionAdaptiveTemplateY, JBIG2StreamDecoder decoder) throws JBIG2Exception, IOException {

		JBIG2Bitmap symbolBitmap;

		int strips = 1 << logStrips;

		clear(defaultPixel);

		//HuffmanDecoder huffDecoder = HuffmanDecoder.getInstance();
		//ArithmeticDecoder arithmeticDecoder = ArithmeticDecoder.getInstance();

		int t;
		if (huffman) {
			t = huffmanDecoder.decodeInt(huffmanDTTable).intResult();
		} else {
			t = arithmeticDecoder.decodeInt(arithmeticDecoder.iadtStats).intResult();
		}
		t *= -strips;

		int currentInstance = 0;
		int firstS = 0;
		int dt, tt, ds, s;
		while (currentInstance < noOfSymbolInstances) {

			if (huffman) {
				dt = huffmanDecoder.decodeInt(huffmanDTTable).intResult();
			} else {
				dt = arithmeticDecoder.decodeInt(arithmeticDecoder.iadtStats).intResult();
			}
			t += dt * strips;

			if (huffman) {
				ds = huffmanDecoder.decodeInt(huffmanFSTable).intResult();
			} else {
				ds = arithmeticDecoder.decodeInt(arithmeticDecoder.iafsStats).intResult();
			}
			firstS += ds;
			s = firstS;

			while (true) {

				if (strips == 1) {
					dt = 0;
				} else if (huffman) {
					dt = decoder.readBits(logStrips);
				} else {
					dt = arithmeticDecoder.decodeInt(arithmeticDecoder.iaitStats).intResult();
				}
				tt = t + dt;

				long symbolID;
				if (huffman) {
					if (symbolCodeTable != null) {
						symbolID = huffmanDecoder.decodeInt(symbolCodeTable).intResult();
					} else {
						symbolID = decoder.readBits(symbolCodeLength);
					}
				} else {
					symbolID = arithmeticDecoder.decodeIAID(symbolCodeLength, arithmeticDecoder.iaidStats);
				}

				if (symbolID >= noOfSymbols) {
					if (JBIG2StreamDecoder.debug)
						System.out.println("Invalid symbol number in JBIG2 text region");
				} else {
					symbolBitmap = null;

					int ri;
					if (symbolRefine) {
						if (huffman) {
							ri = decoder.readBit();
						} else {
							ri = arithmeticDecoder.decodeInt(arithmeticDecoder.iariStats).intResult();
						}
					} else {
						ri = 0;
					}
					if (ri != 0) {

						int refinementDeltaWidth, refinementDeltaHeight, refinementDeltaX, refinementDeltaY;

						if (huffman) {
							refinementDeltaWidth = huffmanDecoder.decodeInt(huffmanRDWTable).intResult();
							refinementDeltaHeight = huffmanDecoder.decodeInt(huffmanRDHTable).intResult();
							refinementDeltaX = huffmanDecoder.decodeInt(huffmanRDXTable).intResult();
							refinementDeltaY = huffmanDecoder.decodeInt(huffmanRDYTable).intResult();

							decoder.consumeRemainingBits();
							arithmeticDecoder.start();
						} else {
							refinementDeltaWidth = arithmeticDecoder.decodeInt(arithmeticDecoder.iardwStats).intResult();
							refinementDeltaHeight = arithmeticDecoder.decodeInt(arithmeticDecoder.iardhStats).intResult();
							refinementDeltaX = arithmeticDecoder.decodeInt(arithmeticDecoder.iardxStats).intResult();
							refinementDeltaY = arithmeticDecoder.decodeInt(arithmeticDecoder.iardyStats).intResult();
						}
						refinementDeltaX = ((refinementDeltaWidth >= 0) ? refinementDeltaWidth : refinementDeltaWidth - 1) / 2 + refinementDeltaX;
						refinementDeltaY = ((refinementDeltaHeight >= 0) ? refinementDeltaHeight : refinementDeltaHeight - 1) / 2 + refinementDeltaY;

						symbolBitmap = new JBIG2Bitmap(refinementDeltaWidth + symbols[(int) symbolID].width, refinementDeltaHeight + symbols[(int) symbolID].height, arithmeticDecoder, huffmanDecoder, mmrDecoder);

						symbolBitmap.readGenericRefinementRegion(template, false, symbols[(int) symbolID], refinementDeltaX, refinementDeltaY, symbolRegionAdaptiveTemplateX, symbolRegionAdaptiveTemplateY);

					} else {
						symbolBitmap = symbols[(int) symbolID];
					}

					int bitmapWidth = symbolBitmap.width - 1;
					int bitmapHeight = symbolBitmap.height - 1;
					if (transposed) {
						switch (referenceCorner) {
						case 0: // bottom left
							combine(symbolBitmap, tt, s, combinationOperator);
							break;
						case 1: // top left
							combine(symbolBitmap, tt, s, combinationOperator);
							break;
						case 2: // bottom right
							combine(symbolBitmap, (tt - bitmapWidth), s, combinationOperator);
							break;
						case 3: // top right
							combine(symbolBitmap, (tt - bitmapWidth), s, combinationOperator);
							break;
						}
						s += bitmapHeight;
					} else {
						switch (referenceCorner) {
						case 0: // bottom left
							combine(symbolBitmap, s, (tt - bitmapHeight), combinationOperator);
							break;
						case 1: // top left
							combine(symbolBitmap, s, tt, combinationOperator);
							break;
						case 2: // bottom right
							combine(symbolBitmap, s, (tt - bitmapHeight), combinationOperator);
							break;
						case 3: // top right
							combine(symbolBitmap, s, tt, combinationOperator);
							break;
						}
						s += bitmapWidth;
					}
				}

				currentInstance++;

				DecodeIntResult decodeIntResult;

				if (huffman) {
					decodeIntResult = huffmanDecoder.decodeInt(huffmanDSTable);
				} else {
					decodeIntResult = arithmeticDecoder.decodeInt(arithmeticDecoder.iadsStats);
				}

				if (!decodeIntResult.booleanResult()) {
					break;
				}

				ds = decodeIntResult.intResult();

				s += sOffset + ds;
			}
		}
	}

	public void clear(int defPixel) {
		data.setAll(defPixel == 1);
		//data.set(0, data.size(), defPixel == 1);
	}

	public void combine(JBIG2Bitmap bitmap, int x, int y, long combOp) {
		int srcWidth = bitmap.width;
		int srcHeight = bitmap.height;
		
//		int maxRow = y + srcHeight;
//		int maxCol = x + srcWidth;
//
//		for (int row = y; row < maxRow; row++) {
//			for (int col = x; col < maxCol; srcCol += 8, col += 8) {
//
//				byte srcPixelByte = bitmap.getPixelByte(srcCol, srcRow);
//				byte dstPixelByte = getPixelByte(col, row);
//				byte endPixelByte;
//
//				switch ((int) combOp) {
//				case 0: // or
//					endPixelByte = (byte) (dstPixelByte | srcPixelByte);
//					break;
//				case 1: // and
//					endPixelByte = (byte) (dstPixelByte & srcPixelByte);
//					break;
//				case 2: // xor
//					endPixelByte = (byte) (dstPixelByte ^ srcPixelByte);
//					break;
//				case 3: // xnor
//					endPixelByte = (byte) ~(dstPixelByte ^ srcPixelByte);
//					break;
//				case 4: // replace
//				default:
//					endPixelByte = srcPixelByte;
//					break;
//				}
//				int used = maxCol - col;
//				if (used < 8) {
//					// mask bits
//					endPixelByte = (byte) ((endPixelByte & (0xFF >> (8 - used))) | (dstPixelByte & (0xFF << (used))));
//				}
//				setPixelByte(col, row, endPixelByte);
//			}
//
//			srcCol = 0;
//			srcRow++;
		int minWidth = srcWidth;
		if (x + srcWidth > width) {
			//Should not happen but occurs sometimes because there is something wrong with halftone pics
			minWidth = width - x;
		}
		if (y + srcHeight > height) {
			//Should not happen but occurs sometimes because there is something wrong with halftone pics
			srcHeight = height - y;
		}

		int srcIndx = 0;
		int indx = y * width + x;
		if (combOp == 0) {
			if (x == 0 && y == 0 && srcHeight == height && srcWidth == width) {
				for (int i=0; i < data.w.length; i++) {
					data.w[i] |= bitmap.data.w[i];
				}
			}
			for (int row = y; row < y + srcHeight; row++) {
				indx = row * width + x;
				data.or(indx, bitmap.data, srcIndx, minWidth);
				/*for (int col = 0; col < minWidth; col++) {
					if (bitmap.data.get(srcIndx + col)) data.set(indx);
					//data.set(indx, bitmap.data.get(srcIndx + col) || data.get(indx));
					indx++;
				}*/
				srcIndx += srcWidth;
			}
		}
		else if (combOp == 1) {
			if (x == 0 && y == 0 && srcHeight == height && srcWidth == width) {
				for (int i=0; i < data.w.length; i++) {
					data.w[i] &= bitmap.data.w[i];
				}
			}
			for (int row = y; row < y + srcHeight; row++) {
				indx = row * width + x;
				for (int col = 0; col < minWidth; col++) {
					data.set(indx, bitmap.data.get(srcIndx + col) && data.get(indx));
					indx++;
				}
				srcIndx += srcWidth;
			}
		}

		else if (combOp == 2) {
			if (x == 0 && y == 0 && srcHeight == height && srcWidth == width) {
				for (int i=0; i < data.w.length; i++) {
					data.w[i] ^= bitmap.data.w[i];
				}
			}
			else {
				for (int row = y; row < y + srcHeight; row++) {
					indx = row * width + x;
					for (int col = 0; col < minWidth; col++) {
						data.set(indx, bitmap.data.get(srcIndx + col) ^ data.get(indx));
						indx++;
					}
					srcIndx += srcWidth;
				}
			}
		}

		else if (combOp == 3) {
			for (int row = y; row < y + srcHeight; row++) {
				indx = row * width + x;
				for (int col = 0; col < minWidth; col++) {
					boolean srcPixel = bitmap.data.get(srcIndx + col);
					boolean pixel = data.get(indx);
					data.set(indx, pixel == srcPixel);
					indx++;
				}
				srcIndx += srcWidth;
			}
		}

		else if (combOp == 4) {
			if (x == 0 && y == 0 && srcHeight == height && srcWidth == width) {
				for (int i=0; i < data.w.length; i++) {
					data.w[i] = bitmap.data.w[i];
				}
			}
			else {
				for (int row = y; row < y + srcHeight; row++) {
					indx = row * width + x;
					for (int col = 0; col < minWidth; col++) {
						data.set(indx, bitmap.data.get(srcIndx + col));
						srcIndx++;
						indx++;
					}
					srcIndx += srcWidth;
				}
			}
		}
	}

	/**
	 * set a full byte of pixels
	 */
//	private void setPixelByte(int col, int row, byte bits) {
		//data.setByte(row, col, bits);
//	}

	/**
	 * get a byte of pixels
	 */
//	public byte getPixelByte(int col, int row) {
		//return data.getByte(row, col);
//	}

	private void duplicateRow(int yDest, int ySrc) {
//		for (int i = 0; i < width;) {
//			setPixelByte(i, yDest, getPixelByte(i, ySrc));
//			i += 8;
//		}
		for (int i = 0; i < width; i++) {
			setPixel(i, yDest, getPixel(i, ySrc));
		}
	}

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}

	public byte[] getData(boolean switchPixelColor) {
//		byte[] bytes = new byte[height * line];
//
//		for (int i = 0; i < height; i++) {
//			System.arraycopy(data.bytes[i], 0, bytes, line * i, line);
//		}
//
//		for (int i = 0; i < bytes.length; i++) {
//			// reverse bits
//
//			int value = bytes[i];
//			value = (value & 0x0f) << 4 | (value & 0xf0) >> 4;
//			value = (value & 0x33) << 2 | (value & 0xcc) >> 2;
//			value = (value & 0x55) << 1 | (value & 0xaa) >> 1;
//
//			if (switchPixelColor) {
//				value ^= 0xff;
//			}
//
//			bytes[i] = (byte) (value & 0xFF);
//		}
//
//		return bytes;
		byte[] bytes = new byte[height * line];

		int count = 0, offset = 0;
		long k = 0;
		for (int row = 0; row < height; row++) {
			for (int col = 0; col < width; col++) {
				if ((count & FastBitSet.mask) == 0) {
					k = data.w[count >>> FastBitSet.pot];
				}
				//if ((k & (1L << count)) != 0) {
					int bit = 7 - (offset & 0x7);
					bytes[offset >> 3] |= ((k >>> count) & 1) << bit;
				//}
				count++;
				offset++;
			}

			offset = (line * 8 * (row + 1));
		}

		if (switchPixelColor) {
			for (int i = 0; i < bytes.length; i++) {
				bytes[i] ^= 0xff;
			}
		}

		return bytes;
	}

	public JBIG2Bitmap getSlice(int x, int y, int width, int height) {
//		JBIG2Bitmap slice = new JBIG2Bitmap(width, height);
//
//		int sliceRow = 0, sliceCol = 0;
//		int maxCol = x + width;
//		
//		//ShowGUIMessage.showGUIMessage("x", this.getBufferedImage(), "xx");
//		
//		System.out.println(">>> getSlice x = "+x+" y = "+y+ " width = "+width+ " height = "+height);
//		System.out.println(">>> baseImage width = "+this.width+ " height = "+this.height);
//		
//		System.out.println("counter = "+counter);
//		if(counter == 17){
//			System.out.println();
//			//ShowGUIMessage.showGUIMessage("x", this.getBufferedImage(), "xx");
//		}
//		
//		ShowGUIMessage.showGUIMessage("x", this.getBufferedImage(), "xx");
//		
//		for (int row = y; row < height; row++) {
//			for (int col = x; col < maxCol; col += 8, sliceCol += 8) {
//				slice.setPixelByte(sliceCol, sliceRow, getPixelByte(col, row));
//				//if(counter > 10)
//					//ShowGUIMessage.showGUIMessage("new", slice.getBufferedImage(), "new");
//			}
//			sliceCol = 0;
//			sliceRow++;
//		}
//		counter++;
//
//		ShowGUIMessage.showGUIMessage("new", slice.getBufferedImage(), "new");
//		
//		return slice;
		
		JBIG2Bitmap slice = new JBIG2Bitmap(width, height, arithmeticDecoder, huffmanDecoder, mmrDecoder);

/*		int sliceRow = 0, sliceCol = 0;
		for (int row = y; row < height; row++) {
			for (int col = x; col < x + width; col++) {
				//System.out.println("row = "+row +" column = "+col);
				//slice.setPixel(sliceCol, sliceRow, getPixel(col, row));
				slice.data.set(sliceRow*slice.width + sliceCol, data.get(row*this.width + col));
				sliceCol++;
			}
			sliceCol = 0;
			sliceRow++;
		}

		return slice;*/
		//int sliceRow = 0, sliceCol = 0;
		int sliceIndx = 0;
		for (int row = y; row < height; row++) {
			int indx = row * this.width + x;
			for (int col = x; col < x + width; col++) {
				if (data.get(indx)) slice.data.set(sliceIndx);
				sliceIndx++;
				indx++;
			}
		}

		return slice;
	}

	/**
	private static void setPixel(int col, int row, FastBitSet data, int value) {
		if (value == 1)
			data.set(row, col);
		else
			data.clear(row, col);
	}/**/

//	private void setPixelByte(int col, int row, FastBitSet data, byte bits) {
//		data.setByte(row, col, bits);
//	}

//	public void setPixel(int col, int row, int value) {
//		setPixel(col, row, data, value);
//	}

//	public int getPixel(int col, int row) {
//		return data.get(row, col) ? 1 : 0;
//	}

	private void setPixel(int col, int row, FastBitSet data, int value) {
		int index = (row * width) + col;

		data.set(index, value == 1);
	}

	public void setPixel(int col, int row, int value) {
		setPixel(col, row, data, value);
	}

	public int getPixel(int col, int row) {
		return data.get((row * width) + col) ? 1 : 0;
	}
	
	public void expand(int newHeight, int defaultPixel) {
//		System.out.println("expand FastBitSet");
//		FastBitSet newData = new FastBitSet(width, newHeight);
//
//		for (int row = 0; row < height; row++) {
//			for (int col = 0; col < width; col += 8) {
//				setPixelByte(col, row, newData, getPixelByte(col, row));
//			}
//		}
//
//		this.height = newHeight;
//		this.data = newData;
		FastBitSet newData = new FastBitSet(newHeight * width);
		for (int row = 0; row < height; row++) {
			for (int col = 0; col < width; col++) {
				setPixel(col, row, newData, getPixel(col, row));
			}
		}

		this.height = newHeight;
		this.data = newData;
	}

	public void setBitmapNumber(int segmentNumber) {
		this.bitmapNumber = segmentNumber;
	}

	public int getBitmapNumber() {
		return bitmapNumber;
	}

	public BufferedImage getBufferedImage() {
		byte[] bytes = getData(true);

		if (bytes == null)
			return null;

		// make a a DEEP copy so we can't alter
		int len = bytes.length;
		byte[] copy = new byte[len];
		System.arraycopy(bytes, 0, copy, 0, len);

		/** create an image from the raw data */
		DataBuffer db = new DataBufferByte(copy, copy.length);

		WritableRaster raster = Raster.createPackedRaster(db, width, height, 1, null);

		BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_BINARY);
		image.setData(raster);

		return image;
	}

	/**
	 * Faster BitSet implementation. Does not perfom any bound checks.
	 *  
	 * @author Boris von Loesch
	 *
	 */
	static final class FastBitSet {
		long[] w;
		static final int pot = (Long.SIZE == 32) ? 5 : (Long.SIZE == 16) ? 4 : (Long.SIZE == 64) ? 6 : 7;
		static final int mask = (int) ((-1L) >>> (Long.SIZE-pot));
		int length;
		
		
		public FastBitSet(int length) {
			this.length = length;
			int wcount = length / Long.SIZE; 
			if (length % Long.SIZE != 0) wcount++;
			w = new long[wcount];
		}
		
		public final int size() {
			return length;
		}

		public void setAll(boolean value) {
			if (value)
				for (int i = 0; i<w.length; i++){
					w[i] = -1L;
				}
			else
				for (int i = 0; i<w.length; i++){
					w[i] = 0;
				}
				
		}
		
		public void set(int start, int end, boolean value) {
			if (value) {
				for (int i=start; i<end; i++) {
					set(i);
				}
			}
			else {
				for (int i=start; i<end; i++) {
					clear(i);
				}				
			}
		}

		public void or(int startindex, final FastBitSet set, int setStartIndex, final int length) {
			final int shift = startindex - setStartIndex;
			long k = set.w[setStartIndex >> pot];
			//Cyclic shift
			k = (k << shift) | (k >>> (Long.SIZE - shift));
			if ((setStartIndex & mask) + length <= Long.SIZE) {
				setStartIndex += shift;
				for (int i=0; i<length; i++) {
					w[(startindex) >>> pot] |= k & (1L << setStartIndex);
					setStartIndex++;
					startindex++;
				}				
			}
			else{
				for (int i=0; i<length; i++) {
					if ((setStartIndex & mask) == 0){ 
						k = set.w[(setStartIndex) >> pot];
						k = (k << shift) | (k >>> (Long.SIZE - shift));
					}
					w[(startindex) >>> pot] |= k & (1L << (setStartIndex+shift));
					setStartIndex++;
					startindex++;
				}
			}
		}
		
		public void set(int index, boolean value) {
			if (value) set(index);
			else clear(index);
		}
		
		public void set(int index) {
			int windex = index >>> pot;
			w[windex] |= (1L<<index);
		}
		
		public void clear(int index) {
			int windex = index >>> pot;
			w[windex] &= ~(1L<<index);
		}

		public final boolean get(int index) {
			int windex = index >>> pot;
			return ((w[windex] & (1L<<index)) != 0);
		}		
	}
}
