package com.cts.autogen.dbGateWay;

import com.cts.autogen.logger.BasicConfApp;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;

public class GetMeteringPointFromDB {


    private final JdbcTemplate jdbcTemplate;
    private final Logger logger = LoggerFactory.getLogger(BasicConfApp.class);

    private String ean;
    private String device;

    public GetMeteringPointFromDB(JdbcTemplate jdbcTemplate){
        this.jdbcTemplate = jdbcTemplate;
    }

    public boolean hasDBEntry(String meter){
        try {
            Pair<String, String> p = jdbcTemplate.queryForObject("select meter_id,meteringPoint from meter_meteringPoint where meter_id=?", (resultSet, i) -> Pair.of(resultSet.getString("meter_id"), resultSet.getString("meteringPoint")), meter);
            assert p != null;
            device = p.getKey();
            ean = p.getValue();
            assert ean != null;
            logger.debug("Got details for meter {}, EAN {}, from internal local H2 DB.",p.getKey(), p.getValue());
            return true;
        }
        catch (EmptyResultDataAccessException ignore){
            logger.info("Could not find meter {} in the base of previously checked meters, will try to get details by API call to nanoMetering.", meter);
            return false;
        }
        catch (IndexOutOfBoundsException e){
            logger.warn("could not get EAN value for meter_id = {}. IndexOutOfBoundsException message below:",meter);
            logger.error("Unable to fetch EAN.",e);
            return false;
        }
        catch (Exception ex){
            logger.warn("could not get EAN value for meter_id = {}. This meter is ignored. General Exception. Exception message below:",meter);
            logger.error("Unable to fetch EAN.",ex);
            return false;
        }
    }

    public void updateH2DB(String device, String ean) {
        logger.debug("will try to insert device {}, EAN {},to local H2 database.",device,ean);
        try {
            jdbcTemplate.update("insert into meter_meteringPoint values (?, ?)", device, ean);
            logger.debug("Inserted meter {}, EAN {} to the H2 database.", device, ean);
        }catch (Exception e){
            logger.warn("Could not update theH2 database. Error message below:");
            logger.error(e.getMessage());
        }
    }

    public String getEan() {
        return ean;
    }

    public String getDevice() {
        return device;
    }
}
