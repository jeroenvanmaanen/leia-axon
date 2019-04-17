import React, { Component } from 'react';
import REST from './rest-client';
import ModelNode from './ModelNode';

class Structure extends Component {
  constructor(props) {
    super(props);
    this.getSymbol = this.getSymbol.bind(this);
    this.state = {
      rootId: null,
      symbolCache: {},
      getSymbol: this.getSymbol
    }
    this.retrieveRootNodeId();
  }

  render() {
    const rootId = this.state.rootId;
    console.log('Render: Root ID:', rootId);
    if (rootId === null) {
      return (<div>No root node</div>);
    } else {
      return (<ModelNode nodeId={rootId} getSymbol={this.state.getSymbol} />);
    }
  }

  async retrieveRootNodeId() {
    const rootId = (await REST("/api/model/structure/root")).entity;
    console.log('Root ID:', rootId);
    this.setState({rootId: rootId});
  }

  async getSymbol(vocabulary, ordinal) {
    const symbolCache = this.state.symbolCache;
    // console.log('Symbol cache keys', Object.keys(symbolCache));
    var vocabularyCache = null;
    if (symbolCache.hasOwnProperty(vocabulary)) {
      vocabularyCache = await (symbolCache[vocabulary]);
    }
    if (vocabularyCache && vocabularyCache.lastOrdinal >= ordinal) {
      // console.log('Use current cache:', vocabulary, ordinal, vocabularyCache);
    } else {
      console.log('New vocabulary cache:', vocabulary, vocabularyCache);
      const vocabularyCacheFuture = this.getVocabularyCache(vocabulary);
      symbolCache[vocabulary] = vocabularyCacheFuture;
      this.setState({symbolCache: symbolCache});
      vocabularyCache = await (symbolCache[vocabulary]);
      console.log('Use new cache:', vocabulary, ordinal, vocabularyCache.lastOrdinal);
    }
    return vocabularyCache.symbols[ordinal];
  }

  async getVocabularyCache(vocabulary) {
    console.log('Fetch new cache:', vocabulary);
    const symbolsList = (await REST('/api/vocabulary/' + vocabulary + '/symbol')).entity;
    const symbols = {};
    var lastOrdinal = 0;
    symbolsList.forEach(symbol => {
      // console.log('Symbol ordinal:', symbol.ordinal);
      symbols[symbol.ordinal] = symbol;
      if (symbol.ordinal > lastOrdinal) {
        lastOrdinal = symbol.ordinal;
      }
    });
    return {
      symbols: symbols,
      lastOrdinal: lastOrdinal
    };
  }
}

export default Structure;
