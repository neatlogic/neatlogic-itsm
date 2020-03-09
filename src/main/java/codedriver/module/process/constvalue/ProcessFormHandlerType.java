package codedriver.module.process.constvalue;

public enum ProcessFormHandlerType {
	INPUT("input"), SELECT("select"), TEXTAREA("textarea"),EDITOR("editor"), RADIO("radio"),CHECKBOX("checkbox"),DATE("date"), TIME("time"),USERSELECT("userselect");
	private String name;

	private ProcessFormHandlerType(String _name) {
		this.name = _name;
	}

	@Override
	public String toString() {
		return this.name;
	}
}
