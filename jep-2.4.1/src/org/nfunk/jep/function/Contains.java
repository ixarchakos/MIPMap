/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.nfunk.jep.function;

//giannisk

import java.util.Stack;
import org.nfunk.jep.ParseException;

public class Contains  extends PostfixMathCommand{
  
    public Contains()
     {
         numberOfParameters = 2;
     }
    
    public void run(Stack inStack) throws ParseException 
	{
            checkStack(inStack);// check the stack
            Object param2 = inStack.pop();
            Object param1 = inStack.pop();
            inStack.push(param1.toString().contains(param2.toString()));//push the result on the inStack
            return; 
	} 
    
}
