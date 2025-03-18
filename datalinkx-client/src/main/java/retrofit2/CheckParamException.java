
package retrofit2;

import lombok.Getter;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.validation.ConstraintViolation;

@Getter
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

	@Override
	public String toString() {
		if (this.errors != null && !this.errors.isEmpty()) {
			return super.toString() + ":" + this.errors.toString();
		} else {
			return super.toString();
		}

	}
}
