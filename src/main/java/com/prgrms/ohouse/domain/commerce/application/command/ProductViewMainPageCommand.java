package com.prgrms.ohouse.domain.commerce.application.command;

import com.prgrms.ohouse.domain.commerce.model.product.Product;
import com.prgrms.ohouse.domain.commerce.model.product.enums.Shipping;

import lombok.Getter;

@Getter
public class ProductViewMainPageCommand {
	private final String name;
	private final int price;
	private final Shipping shipping;
	private final String brand;

	public ProductViewMainPageCommand(Product product) {
		this.name = product.getName();
		this.price = product.getPrice();
		this.shipping = product.getAttribute().getShipping();
		this.brand = product.getAttribute().getBrand();
	}
}
