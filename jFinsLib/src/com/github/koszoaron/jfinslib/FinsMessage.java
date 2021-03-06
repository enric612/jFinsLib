package com.github.koszoaron.jfinslib;

/**
 * A class representing a single FINS message. All objects sent and received through a FINS connections are instances of this class.
 * 
 * @author Aron Koszo <koszoaron@gmail.com>
 */
public class FinsMessage {
    private int[] message = {};
    
    /**
     * Determines the type of the value (hexadecimal or BCD)
     */
    public static enum ValueType {
        /** Store the values as hexadecimal numbers */
        HEX,
        /** Store the values as binary-coded decimals */
        BCD
    }
    
    /** FINS header bytes */
    public static final int[] FINS_HEADER = {0x46, 0x49, 0x4e, 0x53};
    /** Placeholder for the bytes describing the length of the message */
    public static final int[] FINS_LENGTH_PLACEHOLDER = {0x00, 0x00, 0x00, 0x00};
    /** FINS connection command */
    public static final int[] FINS_COMMAND_CONNECT = {0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
    /** FINS generic command */
    public static final int[] FINS_COMMAND_GENERIC = {0x00, 0x00, 0x00, 0x02, 0x00, 0x00, 0x00, 0x00};
    /** FINS connect message payload */
    public static final int[] FINS_CONNECT = {0x00, 0x00, 0x00, 0x00};
    /** The bytes in a FINS message which describe the length of the message. */
    public static final int[] FINS_LENGTH_BYTES = {4, 5, 6, 7};
    /** FINS message command identifier */
    public static final int ICF_COMMAND = 0x80;
    /** FINS message response identifier */
    public static final int ICF_RESPONSE = 0xC0;
    /** FINS reserved byte */
    public static final int RSV = 0x00;
    /** FINS command: read memory area */
    public static int[] CMD_MEMORY_AREA_READ = {0x01, 0x01};
    /** FINS command: write memory area */
    public static int[] CMD_MEMORY_AREA_WRITE = {0x01, 0x02};  
    
    /**
     * Definition of various types of messages.<br>
     * Elements: {@link #CONNECT}, {@link #READ_MEMORY} and {@link #WRITE_MEMORY}
     */
    public static enum MessageType {
        /** The message is a connection command. */
        CONNECT,
        /** The message is a memory reading command. */
        READ_MEMORY,
        /** The message is a memory writing command. */
        WRITE_MEMORY
    }
    
    /**
     * Creates a new FINS message object of the type {@link MessageType#CONNECT}
     */
    public FinsMessage() {
        message = FINS_HEADER;
        append(FINS_LENGTH_PLACEHOLDER);
        append(FINS_COMMAND_CONNECT);
        append(FINS_CONNECT);
        updateMessageLength();
    }
    
    /**
     * Creates a new FINS message object of the type {@link MessageType#READ_MEMORY}
     * 
     * @param memoryArea The memory area designation byte
     * @param registerAddress The address of the register to read from
     * @param length The number of bytes to be read from the register
     */
    public FinsMessage(int memoryArea, int registerAddress, int length) {
        this(MessageType.READ_MEMORY, Constants.DEFAULT_GCT, Constants.DEFAULT_DNA, Constants.DEFAULT_DA1, Constants.DEFAULT_DA2, Constants.DEFAULT_SNA, Constants.DEFAULT_SA1, Constants.DEFAULT_SA2, Constants.DEFAULT_SID, memoryArea, registerAddress, 0, length, null, null);
    }
    
    /**
     * Creates a new FINS message object of the type {@link MessageType#READ_MEMORY}
     * 
     * @param memoryArea The memory area designation byte
     * @param registerAddress The address of the register to read from
     * @param bits The bits of the register to read
     * @param length The number of bytes to be read from the register
     */
    public FinsMessage(int memoryArea, int registerAddress, int bits, int length) {
        this(MessageType.READ_MEMORY, Constants.DEFAULT_GCT, Constants.DEFAULT_DNA, Constants.DEFAULT_DA1, Constants.DEFAULT_DA2, Constants.DEFAULT_SNA, Constants.DEFAULT_SA1, Constants.DEFAULT_SA2, Constants.DEFAULT_SID, memoryArea, registerAddress, bits, length, null, null);
    }
    
    /**
     * Creates a new FINS message object of the type {@link MessageType#WRITE_MEMORY}
     * 
     * @param memoryArea The memory area designation byte
     * @param registerAddress The address of the register to write to
     * @param values The values to write to the register
     * @param valueType The type of values (hex or BCD, if null then hex)
     */
    public FinsMessage(int memoryArea, int registerAddress, int[] values, ValueType valueType) {
        this(MessageType.WRITE_MEMORY, Constants.DEFAULT_GCT, Constants.DEFAULT_DNA, Constants.DEFAULT_DA1, Constants.DEFAULT_DA2, Constants.DEFAULT_SNA, Constants.DEFAULT_SA1, Constants.DEFAULT_SA2, Constants.DEFAULT_SID, memoryArea, registerAddress, 0, 0, values, valueType);
    }
    
    /**
     * Creates a new FINS message object of the type {@link MessageType#WRITE_MEMORY}
     * 
     * @param memoryArea The memory area designation byte
     * @param registerAddress The address of the register to write to
     * @param bits The bits of the register to write
     * @param values The values to write to the register
     * @param valueType The type of values (hex or BCD, if null then hex)
     */
    public FinsMessage(int memoryArea, int registerAddress, int bits, int[] values, ValueType valueType) {
        this(MessageType.WRITE_MEMORY, Constants.DEFAULT_GCT, Constants.DEFAULT_DNA, Constants.DEFAULT_DA1, Constants.DEFAULT_DA2, Constants.DEFAULT_SNA, Constants.DEFAULT_SA1, Constants.DEFAULT_SA2, Constants.DEFAULT_SID, memoryArea, registerAddress, bits, 0, values, valueType);
    }
    
    /**
     * Creates a generic FINS message whose all parameters are customizable
     * 
     * @param type The type of the message ({@link MessageType})
     * @param permissibleNumberOfGateways GCT byte
     * @param destinationNetworkAddress DNA byte
     * @param destinationNodeAddress DA1 byte
     * @param destinationUnitAddress DA2 byte
     * @param sourceNetworkAddress SNA byte
     * @param sourceNodeAddress SA1 byte
     * @param sourceUnitAddress SA2 byte
     * @param sourceId SID byte
     * @param memoryArea The memory are designation byte
     * @param registerAddress The address of the register which is to be accessed
     * @param bits The bits of the register to access
     * @param length The number of bytes to read (nullable on a write command)
     * @param values The values to write (nullable on a read command)
     * @param valueType The type of the values (hex or BCD, if null then hex, nullable on read) 
     */
    public FinsMessage(MessageType type, int permissibleNumberOfGateways, int destinationNetworkAddress, int destinationNodeAddress, int destinationUnitAddress, int sourceNetworkAddress, int sourceNodeAddress, int sourceUnitAddress, int sourceId, int memoryArea, int registerAddress, int bits, int length, int[] values, ValueType valueType) {
        message = FINS_HEADER;
        
        append(FINS_LENGTH_PLACEHOLDER);
        append(FINS_COMMAND_GENERIC);
        append(ICF_COMMAND);
        append(RSV);
        append(permissibleNumberOfGateways);
        append(destinationNetworkAddress);
        append(destinationNodeAddress);
        append(destinationUnitAddress);
        append(sourceNetworkAddress);
        append(sourceNodeAddress);
        append(sourceUnitAddress);
        append(sourceId);
        
        if (type == MessageType.READ_MEMORY) {
            append(CMD_MEMORY_AREA_READ);
            append(memoryArea);
            append(toWord(registerAddress));
            append(bits);
            append(toWord(length));
            
        } else if (type == MessageType.WRITE_MEMORY) {
            append(CMD_MEMORY_AREA_WRITE);
            append(memoryArea);
            append(toWord(registerAddress));
            append(bits);
            append(toWord(values.length));
            
            //TODO check if bcd and convert
            if (valueType == null || valueType == ValueType.HEX) {
                append(toWord(values));
            } else {
                if (bits == 0) {
                    append(toBcdWord(values));
                } else {
                    append(values);
                }
            }
        }
        updateMessageLength();
    }
    
    /**
     * Returns the bytes of the message.
     * 
     * @return An integer array where the elements are the bytes of the message
     */
    public int[] getMessage() {
        return message;
    }
    
    public byte[] getMessageBytes() {
        byte res[] = new byte[message.length];
        for (int i = 0; i < message.length; i++) {
            res[i] = (byte) message[i];
        }
        
        return res;
    }
    
    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        String res = "";
        
        for (int i : message) {
            res += String.format("%02x", i) + " ";
        }
        
        res += "(length: " + message.length + ")";
        
        return res;
    }
    
    /**
     * Appends bytes to the end of the message.
     * 
     * @param bytes An integer array with the bytes to append
     */
    private void append(int[] bytes) {
        int[] res = new int[message.length + bytes.length];
        
        System.arraycopy(message, 0, res, 0, message.length);
        System.arraycopy(bytes, 0, res, message.length, bytes.length);
        
        message = res;
    }
    
    /**
     * Appends a single byte to the end of the message.
     * 
     * @param singleByte The byte to append
     */
    private void append(int singleByte) {
        int[] toAppend = {singleByte};
        append(toAppend);
    }
    
    /**
     * Calculates the length of the messages and writes it to the bytes which store this information.<br>
     * (These bytes are defined by {@link #FINS_LENGTH_BYTES})
     */
    private void updateMessageLength() {
        int length = message.length - 8;
        
        int[] lengthBytes = {
                (byte) (length >>> 24),
                (byte) (length >>> 16),
                (byte) (length >>> 8),
                (byte) length
        };
        
        for (int i : FINS_LENGTH_BYTES) {
            message[i] = lengthBytes[i-4];
        }
    }
    
    /**
     * Converts a number to a word.
     * 
     * @param hexNumber The number to convert
     * @return A two element integer array where the first element represents the upper byte of the word and the second element represents the lower byte of the word
     */
    private int[] toWord(int hexNumber) {
        return new int[] {
                (byte) (hexNumber >>> 8),
                (byte) hexNumber
        };
    }
    
    /**
     * Converts an array of numbers to an array of words.
     * 
     * @param hexNumberArray The array of numbers
     * @return An array of integers which contains pairs of integers. For each pair the first element represents the upper byte of the word and the second element represents the lower byte of the word
     */
    private int[] toWord(int[] hexNumberArray) {
        int[] res = new int[hexNumberArray.length * 2];
        
        for (int i = 0, j = 0; i < res.length; i += 2, j++) {
            int[] word = toWord(hexNumberArray[j]);
            res[i] = word[0];
            res[i+1] = word[1];
        }
        
        return res;
    }
    
    /**
     * Converts a (decimal) number to a BCD word.
     * 
     * @param decNumber A (decimal) number to convert
     * @return A two element integer array where the first element stores the thousands and hundreds while the second element stores the tens and ones
     */
    private int[] toBcdWord(int decNumber) {
        int upper = decNumber / 100;
        int lower = decNumber % 100;
        
        return new int[] {
                16 * (upper / 10) + upper % 10,
                16 * (lower / 10) + lower % 10
        };
    }
    
    /**
     * Converts an array of (decimal) numbers to an array of BCD words.
     * 
     * @param hexNumberArray The array of (decimal) numbers
     * @return An array of integers which contains BCD words. For each word the first byte stores the thousands and hundreds while the second byte stores the tens and ones
     */
    private int[] toBcdWord(int[] decNumberArray) {
        int[] res = new int[decNumberArray.length * 2];
        
        for (int i = 0, j = 0; i < res.length; i += 2, j++) {
            int[] bcdWord = toBcdWord(decNumberArray[j]);
            res[i] = bcdWord[0];
            res[i+1] = bcdWord[1];
        }
        
        return res;
    }
}
