package by.kaminsky.service;

import by.kaminsky.utils.ParseOrder;

import java.util.List;

public interface ParseOrderService {

    List<ParseOrder> prepareParseOrders(String fileName);

    List<ParseOrder> prepareParseOrdersAndCheckForContent(String fileName);

}
