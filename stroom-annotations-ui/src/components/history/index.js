import { connect } from 'react-redux'

import History from './history'

export default connect(
  (state) => ({
     history: state.history
  }),
  null
)(History)
