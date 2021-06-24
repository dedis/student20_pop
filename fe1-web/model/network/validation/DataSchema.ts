import dataCreateLao from 'protocol/query/method/message/data/dataCreateLao.json';
import dataStateLao from 'protocol/query/method/message/data/dataStateLao.json';
import dataUpdateLao from 'protocol/query/method/message/data/dataUpdateLao.json';
import dataCreateMeeting from 'protocol/query/method/message/data/dataCreateMeeting.json';
import dataStateMeeting from 'protocol/query/method/message/data/dataStateMeeting.json';
import dataCreateRollCall from 'protocol/query/method/message/data/dataCreateRollCall.json';
import dataOpenRollCall from 'protocol/query/method/message/data/dataOpenRollCall.json';
import dataCloseRollCall from 'protocol/query/method/message/data/dataCloseRollCall.json';
import dataWitnessMessage from 'protocol/query/method/message/data/dataWitnessMessage.json';
import dataElectionSetup from 'protocol/query/method/message/data/dataElectionSetup.json';
import dataCastVote from 'protocol/query/method/message/data/dataCastVote.json';
import dataElectionEnd from 'protocol/query/method/message/data/dataElectionEnd.json';
import dataElectionResult from 'protocol/query/method/message/data/dataElectionResult.json';

const dataSchemas = [
  dataCreateLao,
  dataStateLao,
  dataUpdateLao,

  dataCreateMeeting,
  dataStateMeeting,

  dataCreateRollCall,
  dataOpenRollCall,
  dataCloseRollCall,

  dataElectionSetup,
  dataCastVote,
  dataElectionEnd,
  dataElectionResult,

  dataWitnessMessage,
];

export default dataSchemas;
