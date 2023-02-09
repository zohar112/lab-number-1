package bgu.spl.net.api;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class MessageEncDecImpl<T> implements MessageEncoderDecoder<T> {
    private int len =0;
    private byte[]bytes = new byte[1<<10];

    @Override
    //to protocol
    public T decodeNextByte(byte nextByte) { //can happen only for 1-9 messages
        if(nextByte == ';')
            return (T) popString();
        pushByte(nextByte);
        return null;
    }

    private String popString() {
        String result="";
        Short sh = bytesToShort(bytes);
        if(sh==01)
            result+= "REGISTER ";
        else if(sh==02)
            result+= "LOGIN ";
        else if(sh==03)
            result+= "LOGOUT ";
        else if(sh==04)
            result+= "FOLLOW ";
        else if(sh==05)
            result+= "POST ";
        else if(sh==06)
            result+= "PM ";
        else if(sh==07)
            result+= "LOGSTAT ";
        else if(sh==8)
            result+= "STAT ";
        else if(sh==12)
            result+= "BLOCK ";

        for (int i=2; i<len; i++) {
            result = result+ new String(bytes, i, 1, StandardCharsets.UTF_8); //changes bit to string charecters
        }
        len =0;
        return result;
    }

    private void pushByte(byte nextByte) {
        if(len >= bytes.length)
            bytes= Arrays.copyOf(bytes,len*2);
        bytes[len++] = nextByte;
    }

    @Override
    public byte[] encode(Object message) {
        String input = (String)message;
        input =input.replaceAll(" ","\0");
        Short sh = null;

        if(input.indexOf("NOTIFICATION")==0) {
            input=input.substring(12);
            sh=9;
        }
        else if(input.indexOf("ACK")==0) {
            input=input.substring(3);
            sh=10;
        }
        else if(input.indexOf("ERROR")==0) {
            input = input.substring(5);
            sh=11;
        }

        byte[] arrShort = shortToBytes(sh);
        byte[] arrByte = (" "+input + ";").getBytes();
        byte[] output = new byte[arrShort.length+arrByte.length];
        output[0] = arrShort[0];
        output[1] = arrShort[1];
        for(int i=2; i<output.length; i++){
            output[i] = arrByte[i-2];
        }
        return output;
    }

    public short bytesToShort(byte[] byteArr) {
        short result = (short) ((byteArr[0] & 0xff) << 8);
        result += (short) (byteArr[1] & 0xff);
        return result;
    }
    public byte[] shortToBytes(short num) {
        byte[] bytesArr = new byte[2];
        bytesArr[0] = (byte)((num >> 8) & 0xFF);
        bytesArr[1] = (byte)(num & 0xFF);
        return bytesArr;
    }

}