import {
    REQUEST_FETCH_ANNOTATION_HISTORY,
    RECEIVE_FETCH_ANNOTATION_HISTORY,
    RECEIVE_FETCH_ANNOTATION_HISTORY_FAILED
} from '../actions/fetchAnnotationHistory'

const defaultState = {
    isFetching: false,
    errorMsg: undefined,
    history: []
}

const annotationHistory = (
    state = defaultState,
    action
 ) => {
    switch(action.type) {
        case REQUEST_FETCH_ANNOTATION_HISTORY:
            return Object.assign({}, state, {
                    isFetching: true,
                    errorMsg: undefined,
                    searchTerm: action.searchTerm
                  })
        case RECEIVE_FETCH_ANNOTATION_HISTORY:
            return Object.assign({}, state, {
                    isFetching: false,
                    errorMsg: undefined,
                    history: action.history,
                    lastUpdated: action.receivedAt
                  })

        case RECEIVE_FETCH_ANNOTATION_HISTORY_FAILED:
            return Object.assign({}, state, {
                    isFetching: false,
                    history: [],
                    lastUpdated: action.receivedAt
                  })
        default:
            return state
    }
}

export default annotationHistory