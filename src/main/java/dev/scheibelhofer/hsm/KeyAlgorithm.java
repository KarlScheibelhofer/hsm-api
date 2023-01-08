package dev.scheibelhofer.hsm;

public enum KeyAlgorithm {
	EC_P256("EC", 256, "secp256r1", "SHA256withECDSA"),
	EC_P384("EC", 384, "secp384r1", "SHA384withECDSA"),
	EC_P521("EC", 521, "secp521r1", "SHA512withECDSA"),
	RSA_PSS_2048("RSA", 2048, null, "RSASSA-PSS"),
	EC_ED25519("Ed25519", 255, "Ed25519", "EdDSA"), // requires Java 17
	EC_ED448("Ed448", 448, "Ed448", "EdDSA"), // requires Java 17
	;

	/* the java key algorithm name, e.g. EC, RSA, Ed25519,... */
	String type;

	/* the key size in bit */
	int size;

	/* the java name for the key generation parameters, used for EC curves */
	String parameterName;

	/* the standard Java algorithm name for signature or encryption */ 
	String preferredAlgorithm;

	private KeyAlgorithm(String type, int size, String paramterName, String preferredAlgorithm) {
		this.type = type;
		this.size = size;
		this.parameterName = paramterName;
		this.preferredAlgorithm = preferredAlgorithm;
	}

}