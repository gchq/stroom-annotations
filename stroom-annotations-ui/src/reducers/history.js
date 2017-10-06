import {
    REQUEST_FETCH_ANNOTATION_HISTORY,
    RECEIVE_FETCH_ANNOTATION_HISTORY,
    RECEIVE_FETCH_ANNOTATION_HISTORY_FAILED
} from '../actions/fetchAnnotationHistory'

const defaultState = []

const history = (
    state = defaultState,
    action
 ) => {
    switch(action.type) {
        case REQUEST_FETCH_ANNOTATION_HISTORY:
        case RECEIVE_FETCH_ANNOTATION_HISTORY_FAILED:
            return []
        case RECEIVE_FETCH_ANNOTATION_HISTORY:
            return action.history
        default:
            return state
    }
}

export default history