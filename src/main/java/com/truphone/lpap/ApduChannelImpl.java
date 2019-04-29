/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.truphone.lpap;

import com.truphone.lpa.ApduChannel;
import com.truphone.lpa.ApduTransmittedListener;
import static com.truphone.util.Util.hexStringToByteArray;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.smartcardio.Card;
import javax.smartcardio.CardChannel;
import javax.smartcardio.CardException;
import javax.smartcardio.CardTerminal;
import javax.smartcardio.CardTerminals;
import javax.smartcardio.CommandAPDU;
import javax.smartcardio.ResponseAPDU;
import javax.smartcardio.TerminalFactory;

/**
 *
 * @author amilcar.pereira
 */
public class ApduChannelImpl implements ApduChannel{
    
    
    private CardChannel basicChannel;//, logicalChannel;
    ApduTransmittedListener apduTransmittedListener;
    
    public ApduChannelImpl(String cardReader) throws CardException{
        //open channel to the card
        TerminalFactory terminalFactory = TerminalFactory.getDefault();
        CardTerminals cardTerminals = terminalFactory.terminals();
        //CardTerminal cardTerminal = cardTerminals.list().get(0);
        CardTerminal cardTerminal = cardTerminals.getTerminal(cardReader);
        Card card = cardTerminal.connect("T=0");
        basicChannel = card.getBasicChannel();
        
        ResponseAPDU responseApdu;
        byte[] apdu;
        
        //send terminal capabilities
        System.out.println("Send Terminal Capabilities");
        apdu = hexStringToByteArray("80AA00000AA9088100820101830107");
        
        responseApdu = basicChannel.transmit(new CommandAPDU(apdu));
        System.out.println(String.format("0x%04X", responseApdu.getSW()));
        
        System.out.println("Open Logical Channel and Select ISD-R");
        //logicalChannel = card.openLogicalChannel();
        
        apdu = hexStringToByteArray("00A4040010A0000005591010FFFFFFFF8900000100");
        
        responseApdu = basicChannel.transmit(new CommandAPDU(apdu));
        System.out.println(String.format("0x%04X", responseApdu.getSW()));
        //transmitAPDU("00A4040010A0000005591010FFFFFFFF8900000100");
        
    }
    
    @Override
    public String transmitAPDU(String apdu) {
        apdu="80" + apdu.substring(2);
        
        ResponseAPDU responseApdu;
        byte[] bApdu;
        
        //send terminal capabilities
        bApdu = hexStringToByteArray(apdu.trim().replaceAll(" ", ""));
        try {
            responseApdu = basicChannel.transmit(new CommandAPDU(bApdu));
            
            if(apduTransmittedListener!=null)
                apduTransmittedListener.onApduTransmitted();
            
        } catch (CardException ex) {
            System.out.println(ex.toString());
            return "";
        }
        
        return byteArrayToHex(responseApdu.getData()) + String.format("%04X", responseApdu.getSW());
    }

    @Override
    public String transmitAPDUS(List<String> apdus) {
        String result="";
        
        for (String apdu : apdus) {
            System.out.println("APDU:" + apdu);
            result = transmitAPDU(apdu);
            
            //if has more than 4 chars for SW, then it contains data. 
            if(result.length()>4)
                return result;
            
            System.out.println("Response:" +result);
        }
        return result;
    }

    @Override
    public void sendStatus() {
        
    }

    @Override
    public void setApduTransmittedListener(ApduTransmittedListener apduTransmittedListener) {
        this.apduTransmittedListener = apduTransmittedListener;
    }

    @Override
    public void removeApduTransmittedListener(ApduTransmittedListener apduTransmittedListener) {
        this.apduTransmittedListener = null;
    }
    
    public void close() throws CardException
    {
        //logicalChannel.close();
        basicChannel.getCard().disconnect(true);
    }
    
     public static String byteArrayToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();

        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
    
}
