package com.citizens.mainframe.model;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;


@Component
@ConfigurationProperties("savingsclosingbalancequeryresponse")
public class SavingsBalanceQueryResponseFormatter extends ResponseBaseModel {

}
