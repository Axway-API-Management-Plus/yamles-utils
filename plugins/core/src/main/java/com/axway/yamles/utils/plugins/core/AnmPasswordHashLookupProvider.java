package com.axway.yamles.utils.plugins.core;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.Map;
import java.util.Optional;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

import org.pf4j.Extension;

import com.axway.yamles.utils.plugins.AbstractBuiltinLookupProvider;
import com.axway.yamles.utils.plugins.FunctionArgument;
import com.axway.yamles.utils.plugins.LookupFunction;
import com.axway.yamles.utils.plugins.LookupFunctionException;
import com.axway.yamles.utils.plugins.LookupProviderException;

@Extension
public class AnmPasswordHashLookupProvider extends AbstractBuiltinLookupProvider {
	public static class PasswordHashGenerator {
		public static final String VERSION = "AAGQAAAAAQAC";

		private PasswordHashGenerator() {
		};

		public static String generate(String password) throws Exception {
			byte[] salt = generateSalt();
			byte[] hash = generateHashedPassord(password, salt);

			String value = new StringBuilder() //
					.append('$').append(VERSION) //
					.append('$').append(Base64.getEncoder().encodeToString(salt)) //
					.append('$').append(Base64.getEncoder().encodeToString(hash)) //
					.toString();
			return value;
		}

		private static byte[] generateSalt() {
			SecureRandom random = new SecureRandom();
			byte[] salt = new byte[16];
			random.nextBytes(salt);

			return salt;
		}

		private static byte[] generateHashedPassord(String password, byte[] salt) throws Exception {
			PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, 102400, 256);
			SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
			return factory.generateSecret(spec).getEncoded();
		}
	}

	protected static class LF extends LookupFunction {

		public LF(String alias, AnmPasswordHashLookupProvider provider) {
			super(alias, provider, AbstractBuiltinLookupProvider.SOURCE);
		}

		@Override
		public Optional<String> lookup(Map<String, Object> args) throws LookupFunctionException {
			String password = getArg(ARG_PWD, args, "");
			if (password == null || password.isEmpty()) {
				return Optional.empty();
			}
			try {
				return Optional.of(PasswordHashGenerator.generate(password));
			} catch (Exception e) {
				throw new LookupFunctionException(this, "ANM password hash generation failed", e);
			}
		}
	}

	protected static FunctionArgument ARG_PWD = new FunctionArgument("pwd", true, "Password", true);

	public AnmPasswordHashLookupProvider() {
		super();
		add(ARG_PWD);
	}

	@Override
	public String getName() {
		return "gen_anm_pwd_hash";
	}

	@Override
	public String getSummary() {
		return "Generate hashed password for Admin Node Manager user.";
	}

	@Override
	public String getDescription() {
		return "Use a clear text password to generate a hashed password to be used for ANM users in 'adminUsers.json' file.";
	}

	@Override
	protected LookupFunction buildFunction() throws LookupProviderException {
		return new LF(getName(), this);
	}
}
