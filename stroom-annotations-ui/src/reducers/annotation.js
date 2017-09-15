import {
    REQUEST_UPDATE_ANNOTATION,
    RECEIVE_UPDATE_ANNOTATION,
    RECEIVE_UPDATE_ANNOTATION_FAILED
} from '../actions/updateAnnotation'

import {
    REQUEST_CREATE_ANNOTATION,
    RECEIVE_CREATE_ANNOTATION,
    RECEIVE_CREATE_ANNOTATION_FAILED
} from '../actions/createAnnotation'

import {
    REQUEST_FETCH_ANNOTATION,
    RECEIVE_FETCH_ANNOTATION,
    RECEIVE_FETCH_ANNOTATION_FAILED
} from '../actions/fetchAnnotation'

import {
    REQUEST_REMOVE_ANNOTATION,
    RECEIVE_REMOVE_ANNOTATION,
    RECEIVE_REMOVE_ANNOTATION_FAILED
} from '../actions/removeAnnotation'

const defaultState = {
    isFetching: false,
    didInvalidate: false,
    errorMsg: undefined
}

const annotation = (
    state = defaultState,
    action
 ) => {
    switch(action.type) {
        case RECEIVE_REMOVE_ANNOTATION:
            return defaultState

        case RECEIVE_REMOVE_ANNOTATION_FAILED:
            return {
                    isFetching: false,
                    didInvalidate: false,
                    errorMsg: action.errorMsg,
                    lastUpdated: action.receivedAt
                  }

        case REQUEST_CREATE_ANNOTATION:
        case REQUEST_UPDATE_ANNOTATION:
        case REQUEST_FETCH_ANNOTATION:
        case REQUEST_REMOVE_ANNOTATION:
            return Object.assign({}, state, {
                    isFetching: true,
                    didInvalidate: false,
                    errorMsg: undefined,
                    annotationId: action.id
                  })

        case RECEIVE_CREATE_ANNOTATION:
        case RECEIVE_UPDATE_ANNOTATION:
        case RECEIVE_FETCH_ANNOTATION:
            return Object.assign({}, state, {
                    isFetching: false,
                    didInvalidate: false,
                    errorMsg: undefined,
                    id: action.id,
                    content: action.content,
                    lastUpdated: action.receivedAt
                  })

        case RECEIVE_CREATE_ANNOTATION_FAILED:
        case RECEIVE_UPDATE_ANNOTATION_FAILED:
            return Object.assign({}, state, {
                    isFetching: false,
                    didInvalidate: false,
                    errorMsg: action.errorMsg,
                    lastUpdated: action.receivedAt
                  })
        case RECEIVE_FETCH_ANNOTATION_FAILED:
            return Object.assign({}, state, {
                    isFetching: false,
                    didInvalidate: false,
                    lastUpdated: action.receivedAt
                  })
        default:
            return state;
    }

}

export default annotation