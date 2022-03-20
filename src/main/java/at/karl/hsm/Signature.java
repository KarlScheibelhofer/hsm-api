package at.karl.hsm;

import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
@RegisterForReflection
public class Signature {
    public Long keyId;
    public String signatureAlgorithm;
    public byte[] signatureValue;
}
