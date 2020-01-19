package com.devxpress.auction.service;

import com.devxpress.auction.api.v1.mapper.BidMapper;
import com.devxpress.auction.api.v1.mapper.BidMapperImpl;
import com.devxpress.auction.api.v1.model.Bid;
import com.devxpress.auction.api.v1.model.BidDetail;
import com.devxpress.auction.entity.BidEntity;
import com.devxpress.auction.repository.BidRepository;
import com.devxpress.auction.repository.BidRepositoryImpl;
import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasProperty;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

public class BidServiceImplTest {

    // Class under test
    private BidServiceImpl bidService;

    private BidRepository spyBidRepository;

    private BidMapper bidMapper = new BidMapperImpl();

    @Before
    public void setUp() {
        spyBidRepository = spy(new BidRepositoryImpl());
        bidService = new BidServiceImpl(spyBidRepository, bidMapper);
    }

    @Test
    public void getAllBids() {

        LocalDateTime now = LocalDateTime.now();

        spyBidRepository.save(createTestBidEntity(1L, "bob", new BigDecimal("12.00"), null));
        spyBidRepository.save(createTestBidEntity(1L, "alice", new BigDecimal("10.00"), null));
        spyBidRepository.save(createTestBidEntity(3L, "alice", new BigDecimal("15.00"), null));
        spyBidRepository.save(createTestBidEntity(1L, "alice", new BigDecimal("11.00"), null));

        List<BidDetail> bids = bidService.getAllBids();

        assertThat(bids, notNullValue());
        assertThat(bids.size(), is(4));

        List<Matcher<? super BidDetail>> bidMatchers1 = new ArrayList<>();
        bidMatchers1.add(hasProperty("itemId", is(1L)));
        bidMatchers1.add(hasProperty("itemDescription", nullValue()));
        bidMatchers1.add(hasProperty("userId", is("bob")));
        bidMatchers1.add(hasProperty("amount", is(new BigDecimal("12.00"))));
        bidMatchers1.add(hasProperty("createdDateTime", greaterThanOrEqualTo(now)));

        List<Matcher<? super BidDetail>> bidMatchers2 = new ArrayList<>();
        bidMatchers2.add(hasProperty("itemId", is(1L)));
        bidMatchers2.add(hasProperty("itemDescription", nullValue()));
        bidMatchers2.add(hasProperty("userId", is("alice")));
        bidMatchers2.add(hasProperty("amount", is(new BigDecimal("10.00"))));
        bidMatchers2.add(hasProperty("createdDateTime", greaterThanOrEqualTo(now)));

        List<Matcher<? super BidDetail>> bidMatchers3 = new ArrayList<>();
        bidMatchers3.add(hasProperty("itemId", is(3L)));
        bidMatchers3.add(hasProperty("itemDescription", nullValue()));
        bidMatchers3.add(hasProperty("userId", is("alice")));
        bidMatchers3.add(hasProperty("amount", is(new BigDecimal("15.00"))));
        bidMatchers3.add(hasProperty("createdDateTime", greaterThanOrEqualTo(now)));

        List<Matcher<? super BidDetail>> bidMatchers4 = new ArrayList<>();
        bidMatchers4.add(hasProperty("itemId", is(1L)));
        bidMatchers4.add(hasProperty("itemDescription", nullValue()));
        bidMatchers4.add(hasProperty("userId", is("alice")));
        bidMatchers4.add(hasProperty("amount", is(new BigDecimal("11.00"))));
        bidMatchers4.add(hasProperty("createdDateTime", greaterThanOrEqualTo(now)));

        assertThat(bids, allOf(
                hasItem(allOf(bidMatchers1)),
                hasItem(allOf(bidMatchers2)),
                hasItem(allOf(bidMatchers3)),
                hasItem(allOf(bidMatchers4))
        ));
    }

    @Test
    public void getBidsForItem() {

        LocalDateTime now = LocalDateTime.now();

        spyBidRepository.save(createTestBidEntity(1L, "bob", new BigDecimal("12.00"), null));
        spyBidRepository.save(createTestBidEntity(1L, "alice", new BigDecimal("10.00"), null));
        spyBidRepository.save(createTestBidEntity(3L, "alice", new BigDecimal("15.00"), null));
        spyBidRepository.save(createTestBidEntity(1L, "alice", new BigDecimal("11.00"), null));

        List<BidDetail> bids = bidService.getBidsForItem(1L);

        assertThat(bids, notNullValue());
        assertThat(bids.size(), is(3));

        List<Matcher<? super BidDetail>> bidMatchers1 = new ArrayList<>();
        bidMatchers1.add(hasProperty("itemId", is(1L)));
        bidMatchers1.add(hasProperty("itemDescription", nullValue()));
        bidMatchers1.add(hasProperty("userId", is("bob")));
        bidMatchers1.add(hasProperty("amount", is(new BigDecimal("12.00"))));
        bidMatchers1.add(hasProperty("createdDateTime", greaterThanOrEqualTo(now)));

        List<Matcher<? super BidDetail>> bidMatchers2 = new ArrayList<>();
        bidMatchers2.add(hasProperty("itemId", is(1L)));
        bidMatchers2.add(hasProperty("itemDescription", nullValue()));
        bidMatchers2.add(hasProperty("userId", is("alice")));
        bidMatchers2.add(hasProperty("amount", is(new BigDecimal("10.00"))));
        bidMatchers2.add(hasProperty("createdDateTime", greaterThanOrEqualTo(now)));

        List<Matcher<? super BidDetail>> bidMatchers3 = new ArrayList<>();
        bidMatchers3.add(hasProperty("itemId", is(1L)));
        bidMatchers3.add(hasProperty("itemDescription", nullValue()));
        bidMatchers3.add(hasProperty("userId", is("alice")));
        bidMatchers3.add(hasProperty("amount", is(new BigDecimal("11.00"))));
        bidMatchers3.add(hasProperty("createdDateTime", greaterThanOrEqualTo(now)));

        assertThat(bids, allOf(
                hasItem(allOf(bidMatchers1)),
                hasItem(allOf(bidMatchers2)),
                hasItem(allOf(bidMatchers3))
        ));
    }

    @Test
    public void getBidsForUser() {
        LocalDateTime now = LocalDateTime.now();

        spyBidRepository.save(createTestBidEntity(1L, "bob", new BigDecimal("12.00"), null));
        spyBidRepository.save(createTestBidEntity(1L, "alice", new BigDecimal("10.00"), null));
        spyBidRepository.save(createTestBidEntity(3L, "alice", new BigDecimal("15.00"), null));
        spyBidRepository.save(createTestBidEntity(1L, "alice", new BigDecimal("11.00"), null));

        List<BidDetail> bids = bidService.getBidsForUser("alice");

        assertThat(bids, notNullValue());
        assertThat(bids.size(), is(3));

        List<Matcher<? super BidDetail>> bidMatchers1 = new ArrayList<>();
        bidMatchers1.add(hasProperty("itemId", is(1L)));
        bidMatchers1.add(hasProperty("itemDescription", nullValue()));
        bidMatchers1.add(hasProperty("userId", is("alice")));
        bidMatchers1.add(hasProperty("amount", is(new BigDecimal("10.00"))));
        bidMatchers1.add(hasProperty("createdDateTime", greaterThanOrEqualTo(now)));

        List<Matcher<? super BidDetail>> bidMatchers2 = new ArrayList<>();
        bidMatchers2.add(hasProperty("itemId", is(3L)));
        bidMatchers2.add(hasProperty("itemDescription", nullValue()));
        bidMatchers2.add(hasProperty("userId", is("alice")));
        bidMatchers2.add(hasProperty("amount", is(new BigDecimal("15.00"))));
        bidMatchers2.add(hasProperty("createdDateTime", greaterThanOrEqualTo(now)));

        List<Matcher<? super BidDetail>> bidMatchers3 = new ArrayList<>();
        bidMatchers3.add(hasProperty("itemId", is(1L)));
        bidMatchers3.add(hasProperty("itemDescription", nullValue()));
        bidMatchers3.add(hasProperty("userId", is("alice")));
        bidMatchers3.add(hasProperty("amount", is(new BigDecimal("11.00"))));
        bidMatchers3.add(hasProperty("createdDateTime", greaterThanOrEqualTo(now)));

        assertThat(bids, allOf(
                hasItem(allOf(bidMatchers1)),
                hasItem(allOf(bidMatchers2)),
                hasItem(allOf(bidMatchers3))
        ));
    }

    @Test
    public void createBid() {

        LocalDateTime now = LocalDateTime.now();

        long itemId = 1L;
        String user = "bob";
        BigDecimal amt = new BigDecimal("23.45");

        Bid dto = createTestBid(itemId, user, amt);
        BidEntity saveBid = createTestBidEntity(itemId, user, amt, null);
        BidEntity addedBid = createTestBidEntity(itemId, user, amt, now.toInstant(ZoneOffset.UTC));
        BidDetail added = bidMapper.bidEntityToBidDetail(addedBid);

        doReturn(addedBid).when(spyBidRepository).save(any(BidEntity.class));

        BidDetail bidDetail = bidService.createBid(dto);

        assertThat(bidDetail, is(added));

        ArgumentCaptor<BidEntity> jpaCaptor = ArgumentCaptor.forClass(BidEntity.class);
        verify(spyBidRepository).save(jpaCaptor.capture());
        verifyNoMoreInteractions(spyBidRepository);

        assertThat(jpaCaptor.getValue(), is(saveBid));
    }

    private Bid createTestBid(long id, String user, BigDecimal amount) {

        Bid b = new Bid();
        b.setItemId(id);
        b.setUserId(user);
        b.setAmount(amount);

        return b;
    }

    private BidEntity createTestBidEntity(long id, String user, BigDecimal amount, Instant inst) {

        BidEntity be = new BidEntity();
        be.setItemId(id);
        be.setUserId(user);
        be.setAmount(amount);
        be.setSubmittedAt(inst);

        return be;
    }

}