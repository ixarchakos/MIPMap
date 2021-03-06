/*****************************************************************************
JEP 2.4.1, Extensions 1.1.1
April 30 2007
(c) Copyright 2007, Nathan Funk and Richard Morris
See LICENSE-*.txt for license information.
 *****************************************************************************/
/* Generated By:JJTree: Do not edit this line. ASTInteger.java */
package org.nfunk.jep;

/**
 * Constant Node
 */
public class ASTConstant extends SimpleNode {

    public static final int STRING = 0;
    public static final int NUMBER = 1;
    
    private Object value;
    private int type;

    public ASTConstant(int id) {
        super(id);
    }

    public ASTConstant(Parser p, int id) {
        super(p, id);
    }

    public void setValue(Object val) {
        value = val;
    }

    public Object getValue() {
        return value;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    /** Accept the visitor. **/
    public Object jjtAccept(ParserVisitor visitor, Object data) throws ParseException {
        return visitor.visit(this, data);
    }

    @Override
    public void accept(IExpressionVisitor visitor) {
        visitor.visitASTConstant(this);
    }

    public String toString() {
        return "Constant: " + getValue(); // rjm needed so sub classes print properly
    }
}
