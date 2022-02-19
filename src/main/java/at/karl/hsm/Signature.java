package at.karl.hsm;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Signature {
    public Long keyId;
    public String signatureAlgorithm;
    public byte[] signatureValue;
}
