/******************************************************************************* 
 * * Copyright 2017 Impetus Infotech.
 * *
 * * Licensed under the Apache License, Version 2.0 (the "License");
 * * you may not use this file except in compliance with the License.
 * * You may obtain a copy of the License at
 * *
 * * http://www.apache.org/licenses/LICENSE-2.0
 * *
 * * Unless required by applicable law or agreed to in writing, software
 * * distributed under the License is distributed on an "AS IS" BASIS,
 * * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * * See the License for the specific language governing permissions and
 * * limitations under the License.
 ******************************************************************************/
package com.impetus.eth.jdbc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.web3j.protocol.core.methods.response.EthBlock.Block;

import com.impetus.blkch.sql.query.Column;
import com.impetus.blkch.sql.query.FunctionNode;
import com.impetus.blkch.sql.query.IdentifierNode;
import com.impetus.blkch.sql.query.SelectItem;
import com.impetus.blkch.sql.query.StarNode;
import com.impetus.eth.parser.Function;
import com.impetus.eth.parser.Utils;

/**
 * The Class BlockResultDataHandler.
 * 
 * @author ashishk.shukla
 * 
 */
public class BlockResultDataHandler implements DataHandler
{
    private static final Logger LOGGER = LoggerFactory.getLogger(BlockResultDataHandler.class);

    /** The column names map. */
    private static HashMap<String, Integer> columnNamesMap = new HashMap<String, Integer>();

    static
    {
        columnNamesMap.put("number", 0);
        columnNamesMap.put("hash", 1);
        columnNamesMap.put("parenthash", 2);
        columnNamesMap.put("nonce", 3);
        columnNamesMap.put("sha3uncles", 4);
        columnNamesMap.put("logsbloom", 5);
        columnNamesMap.put("transactionsroot", 6);
        columnNamesMap.put("stateroot", 7);
        columnNamesMap.put("receiptsroot", 8);
        columnNamesMap.put("author", 9);
        columnNamesMap.put("miner", 10);
        columnNamesMap.put("mixhash", 11);
        columnNamesMap.put("totaldifficulty", 12);
        columnNamesMap.put("extradata", 13);
        columnNamesMap.put("size", 14);
        columnNamesMap.put("gaslimit", 15);
        columnNamesMap.put("gasused", 16);
        columnNamesMap.put("timestamp", 17);
        columnNamesMap.put("transactions", 18);
        columnNamesMap.put("uncles", 19);
        columnNamesMap.put("sealfields", 20);
    }

    public static HashMap<String, Integer> returnColumnNamesMap = new HashMap<>();

    public HashMap<String, Integer> getColumnNamesMap()
    {
        return returnColumnNamesMap;
    }

    @Override
    public ArrayList<List<Object>> convertToObjArray(List rows, List<SelectItem> selItems)
    {
        LOGGER.info("Conversion of Block objects to Result set Objects started");
        ArrayList<List<Object>> result = new ArrayList<>();
        boolean columnsInitialized = false;
        for (Object record : rows)
        {
            Block blockInfo = (Block) record;
            List<Object> returnRec = new ArrayList<>();
            for (SelectItem col : selItems)
            {
                if (col.hasChildType(StarNode.class))
                {

                    returnRec.add(blockInfo.getNumberRaw());
                    returnRec.add(blockInfo.getHash());
                    returnRec.add(blockInfo.getParentHash());
                    returnRec.add(blockInfo.getNonceRaw());
                    returnRec.add(blockInfo.getSha3Uncles());
                    returnRec.add(blockInfo.getLogsBloom());
                    returnRec.add(blockInfo.getTransactionsRoot());
                    returnRec.add(blockInfo.getStateRoot());
                    returnRec.add(blockInfo.getReceiptsRoot());
                    returnRec.add(blockInfo.getAuthor());
                    returnRec.add(blockInfo.getMiner());
                    returnRec.add(blockInfo.getMixHash());
                    returnRec.add(blockInfo.getTotalDifficultyRaw());
                    returnRec.add(blockInfo.getExtraData());
                    returnRec.add(blockInfo.getSizeRaw());
                    returnRec.add(blockInfo.getGasLimitRaw());
                    returnRec.add(blockInfo.getGasUsedRaw());
                    returnRec.add(blockInfo.getTimestampRaw());
                    returnRec.add(blockInfo.getTransactions());
                    returnRec.add(blockInfo.getUncles());
                    returnRec.add(blockInfo.getSealFields());
                    if (!columnsInitialized)
                    {
                        returnColumnNamesMap = columnNamesMap;
                    }
                }
                else if (col.hasChildType(Column.class))
                {
                    String colName = col.getChildType(Column.class, 0).getChildType(IdentifierNode.class, 0).getValue();
                    if (!columnsInitialized)
                    {
                        if (columnNamesMap.containsKey(colName.toLowerCase()))
                        {
                            returnColumnNamesMap.put(colName, returnColumnNamesMap.size());
                        }
                        else
                        {
                            throw new RuntimeException("Column " + colName + " doesn't exist in table");
                        }

                    }

                    returnRec.add(Utils.getBlockColumnValue(blockInfo, colName));
                }
                else if (col.hasChildType(FunctionNode.class))
                {
                    Function computFunc = new Function(rows, columnNamesMap, getTableName());
                    Object computeResult = computFunc.computeFunction(col.getChildType(FunctionNode.class, 0));
                    returnRec.add(computeResult);
                    if (col.hasChildType(IdentifierNode.class))
                    {
                        if (!columnsInitialized)
                        {
                            returnColumnNamesMap.put(col.getChildType(IdentifierNode.class, 0).getValue(),
                                    returnColumnNamesMap.size());
                        }
                    }
                    else if (!columnsInitialized)
                    {
                        returnColumnNamesMap.put(
                                computFunc.createFunctionColName(col.getChildType(FunctionNode.class, 0)),
                                returnColumnNamesMap.size());
                    }
                }
            }
            result.add(returnRec);
            columnsInitialized = true;

        }

        LOGGER.info("Conversion completed. Returning to ResultSet");
        return result;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.impetus.eth.jdbc.DataHandler#getTableName()
     */
    @Override
    public String getTableName()
    {

        return "blocks";
    }

}
