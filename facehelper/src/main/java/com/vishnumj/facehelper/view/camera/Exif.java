package com.vishnumj.facehelper.view.camera;

public class Exif {
    private static final String TAG = "CameraExif";


    public static int getOrientation(byte[] jpeg) {
        if (jpeg == null) {
            return 0;
        }

        int offset = 0;
        int length = 0;


        while (offset + 3 < jpeg.length && (jpeg[offset++] & 0xFF) == 0xFF) {
            int marker = jpeg[offset] & 0xFF;


            if (marker == 0xFF) {
                continue;
            }
            offset++;


            if (marker == 0xD8 || marker == 0x01) {
                continue;
            }

            if (marker == 0xD9 || marker == 0xDA) {
                break;
            }


            length = pack(jpeg, offset, 2, false);
            if (length < 2 || offset + length > jpeg.length) {

                return 0;
            }


            if (marker == 0xE1 && length >= 8 &&
                    pack(jpeg, offset + 2, 4, false) == 0x45786966 &&
                    pack(jpeg, offset + 6, 2, false) == 0) {
                offset += 8;
                length -= 8;
                break;
            }


            offset += length;
            length = 0;
        }


        if (length > 8) {

            int tag = pack(jpeg, offset, 4, false);
            if (tag != 0x49492A00 && tag != 0x4D4D002A) {

                return 0;
            }
            boolean littleEndian = (tag == 0x49492A00);


            int count = pack(jpeg, offset + 4, 4, littleEndian) + 2;
            if (count < 10 || count > length) {

                return 0;
            }
            offset += count;
            length -= count;


            count = pack(jpeg, offset - 2, 2, littleEndian);
            while (count-- > 0 && length >= 12) {

                tag = pack(jpeg, offset, 2, littleEndian);
                if (tag == 0x0112) {

                    int orientation = pack(jpeg, offset + 8, 2, littleEndian);
                    switch (orientation) {
                        case 1:
                            return 0;
                        case 3:
                            return 180;
                        case 6:
                            return 90;
                        case 8:
                            return 270;
                    }

                    return 0;
                }
                offset += 12;
                length -= 12;
            }
        }

        return 0;
    }

    private static int pack(byte[] bytes, int offset, int length,
                            boolean littleEndian) {
        int step = 1;
        if (littleEndian) {
            offset += length - 1;
            step = -1;
        }

        int value = 0;
        while (length-- > 0) {
            value = (value << 8) | (bytes[offset] & 0xFF);
            offset += step;
        }
        return value;
    }
}
