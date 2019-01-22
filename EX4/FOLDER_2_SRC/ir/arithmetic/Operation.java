package ir.arithmetic;

import utils.NotNull;

public enum Operation {
    Plus("+"), Minus("-"), Times("*"), Divide("/"), Equals("=="), GreaterThan("<"), Or("||"), And("&&");

    @NotNull
    String text;

    Operation(@NotNull String text) {
        this.text = text;
    }
}

