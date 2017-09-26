import {
    REQUEST_SEARCH_ANNOTATIONS,
    RECEIVE_SEARCH_ANNOTATIONS,
    RECEIVE_SEARCH_ANNOTATIONS_FAILED
} from '../actions/searchAnnotations'

import {
    RECEIVE_REMOVE_ANNOTATION
} from '../actions/removeAnnotation'

const defaultState = {
    isFetching: false,
    errorMsg: undefined,
    searchTerm: '',
    annotations: []
}

const annotations = (
    state = defaultState,
    action
 ) => {
    switch(action.type) {
        case RECEIVE_REMOVE_ANNOTATION:
            let annotations = state.annotations.filter(a => a.id !== action.id)
            return Object.assign({}, state, {
                    isFetching: false,
                    lastUpdated: action.receivedAt,
                    annotations
                })

        case REQUEST_SEARCH_ANNOTATIONS:
            return Object.assign({}, state, {
                    isFetching: true,
                    errorMsg: undefined,
                    searchTerm: action.searchTerm
                  })

        case RECEIVE_SEARCH_ANNOTATIONS:
            return Object.assign({}, state, {
                    isFetching: false,
                    errorMsg: undefined,
                    annotations: action.annotations,
                    lastUpdated: action.receivedAt
                  })

        case RECEIVE_SEARCH_ANNOTATIONS_FAILED:
            return Object.assign({}, state, {
                    isFetching: false,
                    annotations: [],
                    lastUpdated: action.receivedAt
                  })
        default:
            return state;
    }

}

export default annotations