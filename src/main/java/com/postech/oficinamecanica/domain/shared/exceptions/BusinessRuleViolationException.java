package com.postech.oficinamecanica.domain.shared.exceptions;

public class BusinessRuleViolationException extends DomainException{
    public BusinessRuleViolationException(String ruleMessage) {
        super(ruleMessage);
    }
}
