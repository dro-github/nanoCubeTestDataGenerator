package com.cts.autogen.dbGateWay;

import com.cts.autogen.logger.BasicConfApp;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;

public class GetSettlementMethodFromDB {
    private final JdbcTemplate jdbcTemplate;
    private final Logger logger = LoggerFactory.getLogger(BasicConfApp.class);

    private String ean;
    private String settlementMethod;

    public GetSettlementMethodFromDB(JdbcTemplate jdbcTemplate){
        this.jdbcTemplate = jdbcTemplate;
    }

    public boolean hasDBEntry(String meteringPoint){
        try {
            Pair<String, String> p = jdbcTemplate.queryForObject("select meteringPoint,settlementMethod from meteringPoint_settlementMethod where meteringPoint=?", (resultSet, i) -> Pair.of(resultSet.getString("meteringPoint"), resultSet.getString("settlementMethod")), meteringPoint);
            assert p != null;
            ean = p.getKey();
            settlementMethod = p.getValue();
            assert ean != null;
            logger.debug("Got details for EAN {}, settlementMethod {} from internal local H2 DB.",p.getKey(), p.getValue());
            return true;
        }
        catch (EmptyResultDataAccessException ignore){
            logger.info("Could not find meteringPoint {} in the base of previously checked meters, will try to get details by API call to nanoMetering.", meteringPoint);
            return false;
        }
        catch (IndexOutOfBoundsException e){
            logger.warn("could not get settlementMethod value for meteringPoint = {}. IndexOutOfBoundsException message below:",meteringPoint);
            logger.error("Unable to fetch EAN.",e);
            return false;
        }
        catch (Exception ex){
            logger.warn("could not get settlementMethod value for meteringPoint = {}. This meter is ignored. General Exception. Exception message below:",meteringPoint);
            logger.error("Unable to fetch EAN.",ex);
            return false;
        }
    }

    public void updateH2DB(String meteringPoint, String settlementMethod) {
        logger.debug("will try to insert EAN {}, settlementMethod {} to local H2 database.",meteringPoint,settlementMethod);
        try {
            jdbcTemplate.update("insert into meteringPoint_settlementMethod values (?, ?)", meteringPoint, settlementMethod);
            logger.debug("Inserted EAN {}, settlementMethod {} to the H2 database.", meteringPoint, settlementMethod);
        }catch (Exception e){
            logger.warn("Could not update theH2 database. Error message below:");
            logger.error(e.getMessage());
        }
    }

    public String getSettlementMethod() {
        return settlementMethod;
    }
}
