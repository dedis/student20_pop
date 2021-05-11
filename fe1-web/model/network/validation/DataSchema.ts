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

  dataWitnessMessage,
];

export default dataSchemas;
