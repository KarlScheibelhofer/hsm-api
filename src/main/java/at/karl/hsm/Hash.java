package at.karl.hsm;

import lombok.Data;

@Data
public class Hash {
    public String algorithm;
    public byte[] value;
}
