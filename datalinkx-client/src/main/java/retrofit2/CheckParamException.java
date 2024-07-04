
package retrofit2;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.validation.ConstraintViolation;

public class CheckParamException extends RuntimeException {
	private static final long serialVersionUID = -3921694200794135865L;
	private Map<String, Object> errors;

	public CheckParamException(Map<String, Object> errors) {
		super(errors.toString());
		this.errors = errors;
	}

	public CheckParamException(String message, Set<ConstraintViolation<Object>> errors) {
		super(message);
		this.errors = errors.stream()
				.collect(Collectors.toMap(k -> k.getPropertyPath().toString(), ConstraintViolation::getMessage));
	}

	public Map<String, Object> getErrors() {
		return errors;
	}

	@Override
	public String toString() {
		if (this.errors != null && !this.errors.isEmpty()) {
			return super.toString() + ":" + this.errors.toString();
		} else {
			return super.toString();
		}

	}
}
