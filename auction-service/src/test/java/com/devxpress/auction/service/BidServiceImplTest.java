package com.devxpress.auction.service;

import com.devxpress.auction.api.v1.mapper.BidMapper;
import com.devxpress.auction.api.v1.mapper.BidMapperImpl;
import com.devxpress.auction.repository.BidRepository;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class BidServiceImplTest {

    // Class under test
    private BidServiceImpl bidService;

    @Mock
    private BidRepository bidRepository;

    private BidMapper bidMapper = new BidMapperImpl();

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        bidService = new BidServiceImpl(bidRepository, bidMapper);
    }

    @Test
    public void getAllBids() {
    }

    @Test
    public void getBidsForItem() {
    }

    @Test
    public void getBidsForUser() {
    }

    @Test
    public void createBid() {
    }
}