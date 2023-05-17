package by.kaminsky.service;

import by.kaminsky.helper_objects.ParseOrder;

import java.util.List;

public interface ParseOrderService {

    List<ParseOrder> prepareParseOrders(String fileName);

    List<ParseOrder> prepareParseOrdersAndCheckForContent(String fileName);

}
