import { Lao } from 'model/objects';
import { dispatch, getStore } from '../Storage';
import { connectToLao, makeCurrentLao } from '../reducers';

export namespace OpenedLaoStore {
  let currentLao: any;
  export function store(lao: Lao): void {
    const laoState = lao.toState();
    dispatch(connectToLao(laoState));
  }
  // Consider using an alternative way to access the store wherever possible
  export function get(): Lao {
    if (currentLao === undefined) {
      currentLao = makeCurrentLao();
    }
    const lao = currentLao(getStore().getState());
    if (!lao) {
      throw new Error('Error encountered while accessing storage : no currently opened LAO');
    }
    return lao;
  }
}
