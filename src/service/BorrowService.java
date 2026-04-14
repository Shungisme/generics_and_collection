package service;

import model.BorrowSlip;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BorrowService {
	private final List<BorrowSlip> slips = new ArrayList<>();
	private final Map<String, BorrowSlip> slipById = new HashMap<>();

	public void addSlip(BorrowSlip slip) {
		slips.add(slip);
		slipById.put(slip.getSlipId(), slip);
	}

	public BorrowSlip getById(String slipId) {
		return slipById.get(slipId);
	}

	public List<BorrowSlip> getAll() {
		return new ArrayList<>(slips);
	}
}
