package com.adaptris.core.jdbc;

/**
 * Represents a single INOUT parameter for a Stored Procedure.
 * <p>
 * This parameter will both extract data from the AdaptrisMessage to pass that data as in INOUT parameter to the Stored Procedure
 * and reapply the matching Stored Procedure INOUT parameter data back into the AdaptrisMessage.
 * </p>
 * 
 * @author Aaron McGrath
 * 
 */
public interface InOutParameter extends InParameter, OutParameter {

}
